package me.vitalframework.commands

import jakarta.annotation.PostConstruct
import me.vitalframework.*
import me.vitalframework.commands.VitalCommand.Arg.Type
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.reflect.KClass

abstract class VitalCommand<P, CS : Any> protected constructor(val plugin: P, val commandSenderClass: Class<CS>) :
    RequiresAnnotation<VitalCommand.Info> {
    val name: String
    val permission: String
    val playerOnly: Boolean
    private val args: Map<Pattern, Arg>
    private val argHandlers: Map<Arg, ArgHandlerContext>
    private val argExceptionHandlers: Map<Arg, Map<Class<out Throwable>, ArgExceptionHandlerContext>>

    init {
        val vitalCommandInfo = getRequiredAnnotation()

        name = vitalCommandInfo.name
        permission = vitalCommandInfo.permission
        playerOnly = vitalCommandInfo.playerOnly
        args = VitalCommandUtils.getMappedArgs(this)
        argHandlers = VitalCommandUtils.getMappedArgHandlers(this)
        argExceptionHandlers = VitalCommandUtils.getMappedArgExceptionHandlers(this)
    }

    override fun requiredAnnotationType() = Info::class.java

    abstract fun isPlayer(commandSender: CS): Boolean
    abstract fun hasPermission(commandSender: CS, permission: String): Boolean
    abstract fun getAllPlayerNames(): List<String>

    internal fun getArg(executedArg: String) = args.entries.filter { it.key.matcher(executedArg).matches() }.map { it.value }.firstOrNull()

    private fun executeArgExceptionHandlerMethod(sender: CS, exception: Throwable, executedArg: String, commandArg: Arg) {
        val exceptionHandlers = argExceptionHandlers[commandArg]

        if (exceptionHandlers.isNullOrEmpty()) {
            // we do not have any exception handler mapped for this argument
            // try to find a global exception handler
            val context = VitalCommandExceptionHandlerProcessor.getExceptionHandler(exception::class.java)
                ?: throw RuntimeException(exception)

            try {
                context.handlerMethod(
                    context.adviceInstance,
                    *VitalCommandUtils.getInjectableExceptionHandlerMethodParameters(context, sender, executedArg, commandArg, exception)
                )
            } catch (e: Exception) {
                throw VitalCommandException.ExecuteExceptionHandlerMethod(context.handlerMethod, context, e)
            }

            return
        }
        // we may or may not have an exception handler mapped for this execution context
        val context = exceptionHandlers.entries.filter { it.key.isAssignableFrom(exception.javaClass) }.map { it.value }.firstOrNull()
            ?: return onCommandError(sender, commandArg, exception)

        try {
            context.handlerMethod(
                this,
                *VitalCommandUtils.getInjectableArgExceptionHandlerMethodParameters(context, sender, executedArg, commandArg, exception)
            )
        } catch (e: Exception) {
            throw VitalCommandException.ExecuteArgExceptionHandlerMethod(context.handlerMethod, context, e)
        }
    }

    private fun executeArgHandlerMethod(sender: CS, executedArg: String, commandArg: Arg, values: Array<String>): ReturnState {
        val argHandlerContext = argHandlers[commandArg] ?: throw VitalCommandException.UnmappedArgHandler(executedArg)

        return argHandlerContext.handlerMethod(
            this,
            *VitalCommandUtils.getInjectableArgHandlerMethodParameters(argHandlerContext, sender, executedArg, commandArg, values)
        ) as ReturnState
    }

    fun tabComplete(sender: CS, args: Array<String>): List<String> {
        val tabCompleted = mutableListOf<String>()

        for (arg in this.args.values) {
            // Split the value of the command argument into individual parts.
            val originalArgs = arg.value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // Clone the originalArgs to avoid modification.
            val editedArgs = originalArgs.clone()
            // Check if the originalArgs length is greater than or equal to the provided args length
            // or if the last element of originalArgs ends with "%*".
            if (originalArgs.size < args.size && !originalArgs[originalArgs.size - 1].endsWith("%*")) continue

            for (argIndex in args.indices) {
                // Determine the original argument at the current index.
                val originalArg = if (argIndex >= originalArgs.size) originalArgs[originalArgs.size - 1] else originalArgs[argIndex]

                if (!originalArg.startsWith("%") && !(originalArg.endsWith("%") || originalArg.endsWith("%*"))) continue
                // Replace the edited argument at the corresponding index with the provided argument.
                editedArgs[if (argIndex >= editedArgs.size) editedArgs.size - 1 else argIndex] = args[argIndex]
            }
            // Determine the final argument from originalArgs and args.
            val finalArg = originalArgs[if (args.size - 1 >= originalArgs.size) originalArgs.size - 1 else args.size - 1]
            // Check if the joined editedArgs start with the joined provided args.
            if (!editedArgs.joinToString(" ").startsWith(args.joinToString(" "))) continue

            if (finalArg.startsWith("%") && finalArg.endsWith("%*")) {
                // Add the final argument with "%" and "%*" removed to the tabCompleted list.
                tabCompleted.add(finalArg.replace("%", "").replace("%*", ""))
                continue
            }
            val commandArgType = Type.getTypeByPlaceholder(finalArg)

            when {
                commandArgType != null -> commandArgType.action(TabCompletionContext(tabCompleted, getAllPlayerNames()))
                finalArg.startsWith("%") && (finalArg.endsWith("%") || finalArg.endsWith("%*")) -> tabCompleted.add(
                    finalArg.replace("%", "").replace("%*", "")
                )

                else -> tabCompleted.add(finalArg)
            }
        }
        val formattedArgs = args.joinToString(" ") { "?" }
        val commandTabCompleted = onCommandTabComplete(sender, formattedArgs)
        // when our OWN implementation is not empty, clear all of Vital's defaults.
        if (commandTabCompleted.isNotEmpty()) tabCompleted.clear()
        // finally add further tab-completed suggestions implemented by the developer.
        tabCompleted.addAll(commandTabCompleted)
        return tabCompleted
    }

    fun execute(sender: CS, args: Array<String>) {
        // Check if the command requires a player sender.
        if (playerOnly) {
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
        val commandReturnState = when {
            executingArg == null && joinedPlayerArgs.isBlank() -> try {
                onBaseCommand(sender)
            } catch (e: Exception) {
                onCommandError(sender, null, e)

                return
            }

            executingArg != null -> {
                val values = ArrayList<String>()
                val commandArgs = executingArg.value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.map { it.lowercase() }

                for (arg in args) {
                    if (arg.lowercase() !in commandArgs) values.add(arg)
                }

                try {
                    executeArgHandlerMethod(sender, joinedPlayerArgs, executingArg, values.toTypedArray())
                } catch (e: Exception) {
                    if (e is InvocationTargetException) {
                        executeArgExceptionHandlerMethod(sender, e.targetException, joinedPlayerArgs, executingArg)
                    } else {
                        executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, executingArg)
                    }

                    return
                }
            }

            else -> ReturnState.INVALID_ARGS
        }
        val joinedArgs = args.joinToString(" ")
        // Handle the command return state.
        when (commandReturnState) {
            ReturnState.SUCCESS -> run {}
            ReturnState.INVALID_ARGS -> onCommandInvalidArgs(sender, joinedArgs)
            ReturnState.NO_PERMISSION -> onCommandRequiresPermission(sender, joinedArgs, executingArg)
        }
    }

    protected open fun onCommandError(sender: CS, commandArg: Arg?, e: Throwable) {}
    protected open fun onBaseCommand(sender: CS) = ReturnState.INVALID_ARGS
    protected open fun onCommandTabComplete(sender: CS, args: String) = listOf<String>()
    protected open fun onCommandInvalidArgs(sender: CS, args: String) {}
    protected open fun onCommandRequiresPermission(sender: CS, args: String, commandArg: Arg?) {}
    protected open fun onCommandRequiresPlayer(sender: CS, args: String, commandArg: Arg?) {}

    enum class ReturnState {
        INVALID_ARGS, SUCCESS, NO_PERMISSION
    }

    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        val name: String,
        val description: String = "A Vital Command",
        val aliases: Array<String> = [],
        val usage: String = "",
        val permission: String = "",
        val playerOnly: Boolean = true,
    )

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Arg(val value: String, val permission: String = "", val playerOnly: Boolean = false) {
        enum class Type(val placeholder: String, val action: (TabCompletionContext) -> Unit) {
            PLAYER("%PLAYER%", { context ->
                context.playerNames.filter { it !in context.completions }.forEach { context.completions.add(it) }
            }),
            BOOLEAN("%BOOLEAN%", {
                it.completions.add("true")
                it.completions.add("false")
            }),
            NUMBER("%NUMBER%", { it.completions.add("0") }),
            MATERIAL("%MATERIAL%", { context ->
                Material.entries.map { it.name }.forEach { context.completions.add(it) }
            });

            companion object {
                fun getTypeByPlaceholder(placeholder: String) = entries.firstOrNull { it.placeholder == placeholder }
            }
        }
    }

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class ArgHandler(val arg: Arg)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ArgExceptionHandler(val arg: String, val type: KClass<out Throwable>)

    @Component
    @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Advice(val commandSenderClass: KClass<out Any>)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ExceptionHandler(val type: KClass<out Throwable>)

    data class ExceptionHandlerContext(
        val adviceInstance: Any,
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
        val exceptionIndex: Int?,
    )

    data class TabCompletionContext(val completions: MutableList<String>, val playerNames: List<String>)

    data class ArgHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
    )

    data class ArgExceptionHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val argIndex: Int?,
        val exceptionIndex: Int?,
    )

    abstract class Spigot(plugin: SpigotPlugin) : VitalCommand<SpigotPlugin, SpigotCommandSender>(plugin, SpigotCommandSender::class.java),
        VitalPluginCommand.Spigot {
        @PostConstruct
        fun init() = plugin.getCommand(name)!!.setExecutor(this)

        override fun onCommand(sender: SpigotCommandSender, command: Command, label: String, args: Array<String>): Boolean {
            execute(sender, args)
            return true
        }

        override fun onTabComplete(sender: SpigotCommandSender, command: Command, label: String, args: Array<String>) =
            tabComplete(sender, args)

        override fun isPlayer(commandSender: SpigotCommandSender) = commandSender is SpigotPlayer
        override fun hasPermission(commandSender: SpigotCommandSender, permission: String) = commandSender.hasPermission(permission)
        override fun getAllPlayerNames() = Bukkit.getOnlinePlayers().map { it.name }
    }

    abstract class Bungee(plugin: BungeePlugin) : VitalCommand<BungeePlugin, BungeeCommandSender>(plugin, BungeeCommandSender::class.java) {
        private lateinit var command: VitalPluginCommand.Bungee

        @PostConstruct
        fun init() {
            setupCommand()
            plugin.proxy.pluginManager.registerCommand(plugin, command)
        }

        private fun setupCommand() {
            this.command = object : VitalPluginCommand.Bungee(name) {
                override fun execute(sender: BungeeCommandSender, args: Array<String>) = this@Bungee.execute(sender, args)
                override fun onTabComplete(sender: BungeeCommandSender, args: Array<String>) = this@Bungee.tabComplete(sender, args)
            }
        }

        override fun isPlayer(commandSender: BungeeCommandSender) = commandSender is BungeePlayer
        override fun hasPermission(commandSender: BungeeCommandSender, permission: String) = commandSender.hasPermission(permission)
        override fun getAllPlayerNames() = ProxyServer.getInstance().players.map { it.name }
    }
}