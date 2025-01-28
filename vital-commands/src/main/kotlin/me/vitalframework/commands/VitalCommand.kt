package me.vitalframework.commands

import jakarta.annotation.PostConstruct
import me.vitalframework.*
import me.vitalframework.commands.VitalCommand.Arg.Type
import me.vitalframework.commands.crossplatform.VitalPluginCommand
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * Abstract base class for custom Minecraft commands using the Vital framework.
 * Provides functionality for command execution, tab completion, and argument handling.
 */
abstract class VitalCommand<P, CS : Any> protected constructor(
    val plugin: P,
    val commandSenderClass: Class<CS>,
) : RequiresAnnotation<VitalCommand.Info> {
    val log = logger()
    val name: String
    val permission: String
    val requiresPlayer: Boolean
    val args: Map<Pattern, Arg>
    val argHandlers: Map<Arg, ArgHandlerContext>
    val argExceptionHandlers: Map<Arg, Map<Class<out Throwable>, ArgExceptionHandlerContext>>

    init {
        val vitalCommandInfo = getRequiredAnnotation()

        name = vitalCommandInfo.name
        permission = vitalCommandInfo.permission
        requiresPlayer = vitalCommandInfo.requiresPlayer
        args = getMappedArgs()
        argHandlers = getMappedArgHandlers()
        argExceptionHandlers = getMappedArgExceptionHandlers()
    }

    override fun requiredAnnotationType() = Info::class.java

    private fun getMappedArgs() = javaClass.methods
        .filter { it.isAnnotationPresent(ArgHandler::class.java) }
        .map { it.getAnnotation(ArgHandler::class.java) }
        .associate {
            Pattern.compile(
                it.value.value.replace(" ".toRegex(), "[ ]")
                    .replace("%.+%[*]".toRegex(), "(.+)")
                    .replace("%.+%".toRegex(), "(\\\\S+)")
            ) to it.value
        }

    private fun getMappedArgHandlers() = javaClass.methods
        .filter { ReturnState::class.java.isAssignableFrom(it.returnType) }
        .filter { it.isAnnotationPresent(ArgHandler::class.java) }
        .associate { method ->
            // now we have a viable method ready for handling incoming arguments
            // we just need to filter out the injectable parameters for our method
            // since we only support a handful of injectable params for handler methods...
            val vitalCommandArg = method.getAnnotation(ArgHandler::class.java).value
            var commandSenderIndex: Int? = null
            var executedArgIndex: Int? = null
            var commandArgIndex: Int? = null
            var valuesIndex: Int? = null

            method.parameters.forEach {
                if (commandSenderClass.isAssignableFrom(it.type)) {
                    commandSenderIndex = method.parameters.indexOf(it)
                } else if (String::class.java.isAssignableFrom(it.type)) {
                    executedArgIndex = method.parameters.indexOf(it)
                } else if (Arg::class.java.isAssignableFrom(it.type)) {
                    commandArgIndex = method.parameters.indexOf(it)
                } else if (Array<String>::class.java.isAssignableFrom(it.type)) {
                    valuesIndex = method.parameters.indexOf(it)
                }
            }

            vitalCommandArg to ArgHandlerContext(
                method,
                commandSenderIndex,
                executedArgIndex,
                commandArgIndex,
                valuesIndex
            )
        }

    private fun getMappedArgExceptionHandlers(): Map<Arg, Map<Class<out Throwable>, ArgExceptionHandlerContext>> {
        val mappedArgExceptionHandlers =
            mutableMapOf<Arg, MutableMap<Class<out Throwable>, ArgExceptionHandlerContext>>()

        javaClass.methods
            .filter { it.isAnnotationPresent(ArgExceptionHandler::class.java) }
            .forEach { method ->
                val argExceptionHandler = method.getAnnotation(ArgExceptionHandler::class.java)!!
                val arg = getArg(argExceptionHandler.value)
                    ?: throw RuntimeException("Exception handler mapping failed, arg '${argExceptionHandler.value}' does not exist")

                var commandSenderIndex: Int? = null
                var executedArgIndex: Int? = null
                var commandArgIndex: Int? = null
                var exceptionIndex: Int? = null

                method.parameters.forEach {
                    if (commandSenderClass.isAssignableFrom(it.type)) {
                        commandSenderIndex = method.parameters.indexOf(it)
                    } else if (String::class.java.isAssignableFrom(it.type)) {
                        executedArgIndex = method.parameters.indexOf(it)
                    } else if (Arg::class.java.isAssignableFrom(it.type)) {
                        commandArgIndex = method.parameters.indexOf(it)
                    } else if (Exception::class.java.isAssignableFrom(it.type)) {
                        exceptionIndex = method.parameters.indexOf(it)
                    }
                }

                val argExceptionHandlerContext = ArgExceptionHandlerContext(
                    method,
                    commandSenderIndex,
                    executedArgIndex,
                    commandArgIndex,
                    exceptionIndex
                )
                if (!mappedArgExceptionHandlers.containsKey(arg)) {
                    mappedArgExceptionHandlers[arg] =
                        mutableMapOf(argExceptionHandler.type.java to argExceptionHandlerContext)
                } else {
                    mappedArgExceptionHandlers[arg]!![argExceptionHandler.type.java] = argExceptionHandlerContext
                }
            }

        return mappedArgExceptionHandlers
            .map { it.key to it.value.toMap() }
            .toMap()
    }

    private fun getArg(arg: String) = args.entries
        .filter { it.key.matcher(arg).matches() }
        .map { it.value }
        .firstOrNull()

    private fun executeArgExceptionHandlerMethod(sender: CS, exception: Throwable, arg: String, commandArg: Arg) {
        val exceptionHandlers = argExceptionHandlers[commandArg]

        if (exceptionHandlers.isNullOrEmpty()) {
            // we do not have any exception handler mapped for this argument
            throw RuntimeException(exception)
        } else {
            val exceptionHandlerContext = exceptionHandlers.entries
                .filter {
                    it.key.isAssignableFrom(
                        exception.javaClass
                    )
                }
                .map { it.value }
                .firstOrNull()

            // we may or may not have an exception handler mapped for this execution context
            if (exceptionHandlerContext == null) {
                // if we don't have an exception handler mapped, call the base exception handler for this command
                onCommandError(sender, commandArg, exception)

                return
            }

            // if we do have an exception, prepare for parameter injection...
            val injectableParameters = mutableMapOf<Int, Any>()

            if (exceptionHandlerContext.commandSenderIndex != null) {
                injectableParameters[exceptionHandlerContext.commandSenderIndex] = sender
            }

            if (exceptionHandlerContext.executedArgIndex != null) {
                injectableParameters[exceptionHandlerContext.executedArgIndex] = arg
            }

            if (exceptionHandlerContext.argIndex != null) {
                injectableParameters[exceptionHandlerContext.argIndex] = commandArg
            }

            if (exceptionHandlerContext.exceptionIndex != null) {
                injectableParameters[exceptionHandlerContext.exceptionIndex] = exception
            }

            try {
                val sortedParameters =
                    injectableParameters.entries
                        .sortedBy { it.key }
                        .map { it.value }
                        .toTypedArray()

                exceptionHandlerContext.handlerMethod.invoke(this, *sortedParameters)
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Error while executing exception handler method using context '$exceptionHandlerContext'")
            }
        }
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    private fun executeArgHandlerMethod(sender: CS, arg: String, commandArg: Arg, values: Array<String>): ReturnState {
        val argHandlerContext = argHandlers[commandArg]
            ?: throw RuntimeException("No handler method exists for arg '$arg'")

        val injectableParameters = mutableMapOf<Int, Any>()

        if (argHandlerContext.commandSenderIndex != null) {
            injectableParameters[argHandlerContext.commandSenderIndex] = sender
        }

        if (argHandlerContext.executedArgIndex != null) {
            injectableParameters[argHandlerContext.executedArgIndex] = arg
        }

        if (argHandlerContext.commandArgIndex != null) {
            injectableParameters[argHandlerContext.commandArgIndex] = commandArg
        }

        if (argHandlerContext.valuesIndex != null) {
            injectableParameters[argHandlerContext.valuesIndex] = values
        }

        val sortedParameters =
            injectableParameters.entries
                .sortedBy { it.key }
                .map { it.value }
                .toTypedArray()

        return argHandlerContext.handlerMethod.invoke(this, *sortedParameters) as ReturnState
    }

    protected fun handleTabComplete(sender: CS, args: Array<String>): List<String> {
        val tabCompleted = ArrayList<String>()

        this.args.values.forEach {
            // Split the value of the command argument into individual parts.
            val originalArgs = it.value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // Clone the originalArgs to avoid modification.
            val editedArgs = originalArgs.clone()

            // Check if the originalArgs length is greater than or equal to the provided args length
            // or if the last element of originalArgs ends with "%*".
            if (originalArgs.size >= args.size || originalArgs[originalArgs.size - 1].endsWith("%*")) {
                args.indices.forEach {
                    // Determine the original argument at the current index.
                    val originalArg =
                        when (it >= originalArgs.size) {
                            true -> originalArgs[originalArgs.size - 1]
                            false -> originalArgs[it]
                        }

                    if (!originalArg.startsWith("%") && !(originalArg.endsWith("%") || originalArg.endsWith("%*"))) {
                        return@forEach
                    }

                    // Replace the edited argument at the corresponding index with the provided argument.
                    editedArgs[
                        when (it >= editedArgs.size) {
                            true -> editedArgs.size - 1
                            false -> it
                        }
                    ] = args[it]
                }

                // Determine the final argument from originalArgs and args.
                val finalArg =
                    originalArgs[
                        when (args.size - 1 >= originalArgs.size) {
                            true -> originalArgs.size - 1
                            false -> args.size - 1
                        }
                    ]

                // Check if the joined editedArgs start with the joined provided args.
                if (!editedArgs.joinToString(" ").startsWith(args.joinToString(" "))) {
                    return@forEach
                }

                if (finalArg.startsWith("%") && finalArg.endsWith("%*")) {
                    // Add the final argument with "%" and "%*" removed to the tabCompleted list.
                    tabCompleted.add(finalArg.replace("%", "").replace("%*", ""))

                    return@forEach
                }

                val commandArgType = Type.getTypeByPlaceholder(finalArg)

                if (commandArgType != null) {
                    commandArgType.action(
                        TabCompletionContext(
                            tabCompleted,
                            getAllPlayerNames()
                        )
                    )
                } else if (finalArg.startsWith("%") && (finalArg.endsWith("%") || finalArg.endsWith("%*"))) {
                    tabCompleted.add(finalArg.replace("%", "").replace("%*", ""))
                } else {
                    tabCompleted.add(finalArg)
                }
            }
        }

        val formattedArgs = args.joinToString(" ") { "?" }
        val commandTabCompleted = onCommandTabComplete(sender, formattedArgs)

        // when our OWN implementation is not empty, clear all of Vital's defaults.
        if (commandTabCompleted.isNotEmpty()) {
            tabCompleted.clear()
        }

        // finally add further tab-completed suggestions implemented by the developer.
        tabCompleted.addAll(commandTabCompleted)

        return tabCompleted
    }

    abstract fun isPlayer(commandSender: CS): Boolean

    abstract fun hasPermission(commandSender: CS, permission: String): Boolean

    abstract fun getAllPlayerNames(): List<String>

    fun execute(sender: CS, args: Array<String>) {
        // Check if the command requires a player sender.
        if (requiresPlayer) {
            // Check if the sender is not a Player.
            if (!isPlayer(sender)) {
                // Execute the onCommandRequiresPlayer method and return true.
                onCommandRequiresPlayer(sender, args.joinToString(" "), null)

                return
            }
        }

        // Check if a permission is required and if the sender has it.
        if (permission.isNotBlank() && !hasPermission(sender, permission)) {
            // Execute the onCommandRequiresPermission method and return true.
            onCommandRequiresPermission(sender, args.joinToString(" "), null)

            return
        }

        // the arguments the player has typed in chat, joined to one single string separated by spaces
        val joinedPlayerArgs = args.joinToString(" ")
        val executingArg = getArg(joinedPlayerArgs)

        // if the player has not put in any arguments, we may execute the base command handler method
        val commandReturnState =
            if (executingArg == null && joinedPlayerArgs.isBlank()) {
                try {
                    onBaseCommand(sender)
                } catch (e: Exception) {
                    onCommandError(sender, null, e)

                    return
                }
            } else if (executingArg != null) {
                val values = ArrayList<String>()

                executingArg.value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.forEach { commandArg ->
                    args.forEach {
                        if (!it.equals(commandArg, ignoreCase = true)) {
                            // we have a custom arg
                            values.add(it)
                        }
                    }
                }

                try {
                    executeArgHandlerMethod(
                        sender,
                        joinedPlayerArgs,
                        executingArg,
                        values.toTypedArray()
                    )
                } catch (e: Exception) {
                    if (e is InvocationTargetException) {
                        executeArgExceptionHandlerMethod(sender, e.targetException, joinedPlayerArgs, executingArg)
                    } else {
                        executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, executingArg)
                    }

                    return
                }
            } else {
                // we have neither executed the base command argument by passing an empty value, or any mapped argument
                ReturnState.INVALID_ARGS
            }

        val joinedArgs = args.joinToString(" ")

        // Handle the command return state.
        when (commandReturnState) {
            ReturnState.SUCCESS -> {}
            ReturnState.INVALID_ARGS -> onCommandInvalidArgs(sender, joinedArgs)
            ReturnState.NO_PERMISSION -> onCommandRequiresPermission(sender, joinedArgs, executingArg)
        }
    }

    /**
     * called when an exception occurs during command execution, that is either thrown on the base command, or not handled by any arg exception handler
     */
    protected open fun onCommandError(sender: CS, arg: Arg?, e: Throwable) {
    }

    /**
     * called when this command is executed with only the base command (/commandname)
     *
     * @param sender the sender
     * @return the status of this command execution
     */
    protected open fun onBaseCommand(sender: CS): ReturnState {
        return ReturnState.INVALID_ARGS
    }

    /**
     * Called upon requesting any tab-completion content.
     *
     * @param sender The [CS] that sent the command.
     * @param args   The arguments used in chat.
     * @return A [List] of strings to show to the player as tab-completion suggestions.
     */
    protected open fun onCommandTabComplete(sender: CS, args: String): List<String> {
        return listOf()
    }

    /**
     * Called when this VitalCommand has been executed using invalid Arguments
     *
     * @param sender The CommandSender
     */
    protected open fun onCommandInvalidArgs(sender: CS, args: String) {
    }

    /**
     * Called when this VitalCommand has been executed without needed Permissions
     *
     * @param sender The CommandSender
     */
    protected open fun onCommandRequiresPermission(sender: CS, args: String, arg: Arg?) {
    }

    /**
     * Called when this VitalCommand has been executed as a non Player Object while requiring a Player to be executed
     *
     * @param sender The CommandSender
     */
    protected open fun onCommandRequiresPlayer(sender: CS, args: String, arg: Arg?) {
    }

    /**
     * Enum representing possible return states for [VitalCommand].
     * Defines different states that a command execution can result in.
     *
     * @author xRa1ny
     */
    enum class ReturnState {
        /**
         * Indicates that the command was executed with invalid arguments.
         */
        INVALID_ARGS,

        /**
         * Indicates that the command execution was successful.
         */
        SUCCESS,

        /**
         * Indicates that the sender did not have permission to execute the command.
         */
        NO_PERMISSION
    }

    /**
     * Annotation used to provide metadata for [VitalCommand].
     *
     * @author xRa1ny
     * @apiNote If combined with the :vital-core-processor and :vital-commands-processor dependency as annotation processor, can automatically define all commands in plugin.yml during compile-time.
     */
    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        /**
         * Defines the name of this command, excluding the slash /.
         *
         * @return The name of the command.
         */
        val name: String,
        /**
         * Defines the description of this command.
         *
         * @return The description of this command.
         */
        val description: String = "A Vital Command",
        /**
         * The aliases of this command.
         *
         * @return The aliases of this command.
         */
        val aliases: Array<String> = [],
        /**
         * The usages message of this command.
         *
         * @return The usages message of this command.
         */
        val usage: String = "",
        /**
         * Defines the permission required to run this command.
         *
         * @return The required permission (default is an empty string).
         */
        val permission: String = "",
        /**
         * Defines if this command can only be executed by a player.
         *
         * @return True if the command requires a player; false otherwise (default is true).
         */
        val requiresPlayer: Boolean = true,
    )

    /**
     * Annotation used to define arguments for [VitalCommand].
     * Arguments may be placeholders that can be used within methods annotated with [ArgHandler].
     * For example, "%PLAYER%" will be replaced with all player names on the server.
     *
     * @author xRa1ny
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Arg(
        /**
         * Placeholder value for the command argument.
         *
         * @return The value of the command argument.
         * @see Type
         */
        val value: String,
        /**
         * Optional permission node associated with the command argument.
         * Defines the required permission to use this argument in a command.
         *
         * @return The permission node (default is an empty string).
         */
        val permission: String = "",
        /**
         * Flag indicating if this argument is specific to players.
         *
         * @return True if the argument is for players only; false otherwise (default is false).
         * @apiNote If set to true, the argument is only applicable to player senders.
         */
        val player: Boolean = false,
    ) {
        /**
         * Patterns used by [Arg] implementations that will be replaced during tab-completion, automatically.
         */
        enum class Type(
            val placeholder: String,
            val action: (TabCompletionContext) -> Unit,
        ) {
            PLAYER("%PLAYER%", { context ->
                context.playerNames // Check if the player name is already in the tabCompleted list.
                    .filter { it !in context.completions }
                    .forEach { context.completions.add(it) }
            }),
            BOOLEAN("%BOOLEAN%", {
                it.completions.add("true")
                it.completions.add("false")
            }),
            NUMBER("%NUMBER%", {
                it.completions.add("0")
            }),
            MATERIAL(
                "%MATERIAL%", { context ->
                    Material.entries
                        .map { it.name }
                        .forEach { context.completions.add(it) }
                });

            companion object {
                fun getTypeByPlaceholder(placeholder: String) = entries.firstOrNull { it.placeholder == placeholder }
            }
        }
    }

    /**
     * Annotation used to specify methods as handlers for [Arg].
     * Handlers are responsible for processing specific command argument values.
     * This annotation helps map methods to their corresponding command argument values.
     *
     * @author xRa1ny
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    annotation class ArgHandler(
        /**
         * Array of command argument values that this handler method processes.
         * Each value corresponds to a specific command argument.
         *
         * @return An array of command argument values.
         */
        val value: Arg,
    )

    /**
     * Decorator annotation for marking exception handling methods for registered commands beans
     */
    @Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ArgExceptionHandler(
        /**
         * Defines the command arg this exception handling method should be mapped to
         */
        val value: String,
        /**
         * Defines the exception type this exception handler should manage
         */
        val type: KClass<out Throwable>,
    )

    data class TabCompletionContext(
        val completions: MutableList<String>,
        val playerNames: List<String>,
    )

    class ArgHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
    )

    class ArgExceptionHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val argIndex: Int?,
        val exceptionIndex: Int?,
    )

    abstract class Spigot(plugin: SpigotPlugin) :
        VitalCommand<SpigotPlugin, SpigotCommandSender>(plugin, SpigotCommandSender::class.java),
        VitalPluginCommand.Spigot {
        @PostConstruct
        fun init() {
            plugin.getCommand(name)!!.setExecutor(this)
        }

        override fun onCommand(
            sender: SpigotCommandSender,
            command: Command,
            label: String,
            args: Array<String>,
        ): Boolean {
            execute(sender, args)

            return true
        }

        override fun onTabComplete(
            sender: SpigotCommandSender,
            command: Command,
            label: String,
            args: Array<String>,
        ) = handleTabComplete(sender, args)

        override fun isPlayer(commandSender: SpigotCommandSender) = commandSender is SpigotPlayer

        override fun hasPermission(commandSender: SpigotCommandSender, permission: String) =
            commandSender.hasPermission(permission)

        override fun getAllPlayerNames() = Bukkit.getOnlinePlayers()
            .map { it.name }
    }

    abstract class Bungee(plugin: BungeePlugin) :
        VitalCommand<BungeePlugin, BungeeCommandSender>(plugin, BungeeCommandSender::class.java) {
        private lateinit var command: VitalPluginCommand.Bungee

        @PostConstruct
        fun init() {
            setupCommand()
            plugin.proxy.pluginManager.registerCommand(plugin, command)
        }

        private fun setupCommand() {
            // wrap a custom bungeecord command class, since in bungeecord, command classes MUST BE EXTENDED FROM.
            // extending is not possible here since the VitalCommand object MUST BE A class, and classes CANNOT HAVE MULTIPLE EXTEND STATEMENTS...
            this.command = object : VitalPluginCommand.Bungee(name) {
                override fun execute(sender: BungeeCommandSender, args: Array<String>) {
                    this@Bungee.execute(sender, args)
                }

                override fun onTabComplete(
                    sender: BungeeCommandSender,
                    args: Array<String>,
                ): Iterable<String> {
                    return this@Bungee.handleTabComplete(sender, args)
                }
            }
        }

        override fun isPlayer(commandSender: BungeeCommandSender) = commandSender is BungeePlayer

        override fun hasPermission(commandSender: BungeeCommandSender, permission: String) =
            commandSender.hasPermission(permission)

        override fun getAllPlayerNames() = ProxyServer.getInstance().players
            .map { it.name }
    }
}