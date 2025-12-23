package me.vitalframework.commands

import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotCommandSender
import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalCoreSubModule.Companion.getRequiredAnnotation
import me.vitalframework.commands.VitalCommandsSubModule.Companion.extractNonInvocationTargetException
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * Defines a command within the Vital-Framework.
 * A command defines n-amount of entry points for user-input, that can perform actions and produce a result.
 *
 * Each command may have n-amount of arguments, each argument may also have n-amount of argument-exception-handlers,
 * mapped to them to handle exceptions during argument execution.
 *
 * You can also define global exception handlers, which can handle exceptions globally, not tied to any specific argument.
 *
 * Each argument is defined as a function using the [VitalCommand.Arg] annotation.
 * An argument may be a list of strings separated by spaces, containing the hard-coded static arguments, and variable ones with either %NAME% or <name> syntax.
 *
 * Each argument handler supports automatic parameter injection for the following parameters:
 * - [CommandSender] or [Player]
 * - [String] for the actual executed argument
 * - [VitalCommand.Arg] for the matched command argument annotation, you have defined within your class.
 * - [Array] of [String] for arguments, that have defined variables using either %NAME% or <name>.
 *
 * Any command related operation is executed in the following order:
 * 1. Look for and execute (if found) argument function based on the user-input.
 * 2. If not exception occurs during this command execution, return.
 * 3. If an exception occurs during execution within the argument, look for and execute (if found) an argument exception handler matching the executed argument and exception type.
 * 4. If no argument exception handler was found during argument execution, look for and execute (if found) a global exception handler for the thrown exception type.
 * 5. If no global exception handler was found for the exception type, fall back to [onCommandError].
 *
 * ```java
 * @VitalCommand.Info(
 *   name = "mycommand",
 *   description = "description"
 * )
 * public class MyCommand extends VitalCommand.Spigot {
 *   // This will define a valid argument a player or command sender can execute
 *   @Arg(name = "static %dynamic% <dynamic> arg")
 *   public ReturnState onSomeArgument(CommandSender sender, String executedArg, Arg commandArg, String[] values) {
 *     // If the command sender executes "/mycommand static 123 456 arg", this function will be called.
 *     // the "values" parameter will have the following values: ["123", "456"]
 *     return ReturnState.SUCCESS;
 *   }
 * }
 * ```
 */
abstract class VitalCommand<CS : Any> protected constructor(
    val commandSenderClass: Class<CS>,
) {
    /**
     * The name of this command, defined by [VitalCommand.Info.name].
     */
    val name: String

    /**
     * The permission to execute this base command (no arguments), defined by [VitalCommand.Info.permission].
     */
    val permission: String

    /**
     * If this base command (no arguments) should only be executable by a player, defined by [VitalCommand.Info.playerOnly].
     */
    val playerOnly: Boolean

    /**
     * The arguments of the command, mapped from [Pattern] to [VitalCommand.Arg].
     */
    val args: Map<Pattern, Arg>

    /**
     * The [VitalCommand.ArgHandlerContext] mapped by their [VitalCommand.Arg].
     */
    val argHandlers: Map<Arg, ArgHandlerContext>

    /**
     * The [Throwable] (type of exception) and their [VitalCommand.ArgExceptionHandlerContext] mapped by their [VitalCommand.Arg].
     */
    val argExceptionHandlers: Map<Arg, Map<Class<out Throwable>, ArgExceptionHandlerContext>>

    init {
        val info = getInfo()
        name = info.name
        permission = info.permission
        playerOnly = info.playerOnly
        args = getMappedArgs()
        argHandlers = getMappedArgHandlers()
        argExceptionHandlers = getMappedArgExceptionHandlers()
    }

    /**
     * Determines if the given command sender is a player.
     */
    abstract fun isPlayer(commandSender: CS): Boolean

    /**
     * Checks whether the specified command sender has the required permission.
     */
    abstract fun hasPermission(
        commandSender: CS,
        permission: String,
    ): Boolean

    /**
     * Retrieves a list of all player names.
     */
    abstract fun getAllPlayerNames(): List<String>

    /**
     * Retrieves a [VitalCommand.Arg] based on the executed argument string.
     */
    internal fun getArg(executedArg: String) =
        args.entries
            .filter { it.key.matcher(executedArg).matches() }
            .map { it.value }
            .firstOrNull()

    /**
     * Executes the global exception handler method for handling exceptions thrown during command execution.
     */
    private fun executeGlobalExceptionHandlerMethod(
        sender: CS,
        executedArg: String,
        commandArg: Arg?,
        originalException: Throwable,
    ) {
        val exception = originalException.extractNonInvocationTargetException()

        val globalContext =
            VitalGlobalCommandExceptionHandlerProcessor.getGlobalExceptionHandler(exception.javaClass)
                ?: try {
                    return onCommandError(sender, commandArg, exception)
                } catch (e: Exception) {
                    // try to find a global exception handler mapped for the exception that was thrown while handling a command error
                    val secondGlobalContext =
                        VitalGlobalCommandExceptionHandlerProcessor.getGlobalExceptionHandler(e.javaClass)
                            ?: // if we couldn't find a global exception handler for this exception, we are out of options...
                            // simple rethrow this exception so it is redirected to default exception handling...
                            throw e

                    // if we DO have a second global exception handler for this exception, we can call it here...
                    secondGlobalContext.handlerMethod(
                        secondGlobalContext.adviceInstance,
                        *secondGlobalContext.getInjectableGlobalExceptionHandlerMethodParameters(
                            sender,
                            executedArg,
                            commandArg,
                            e,
                        ),
                    )
                    return
                }

        try {
            globalContext.handlerMethod(
                globalContext.adviceInstance,
                *globalContext.getInjectableGlobalExceptionHandlerMethodParameters(
                    sender,
                    executedArg,
                    commandArg,
                    exception,
                ),
            )
            return
        } catch (e: Exception) {
            throw VitalCommandException.ExecuteGlobalExceptionHandlerMethod(
                globalContext.handlerMethod,
                globalContext,
                e,
            )
        }
    }

    /**
     * Executes an exception handler registered for a specific command argument.
     */
    private fun executeArgExceptionHandlerMethod(
        sender: CS,
        originalException: Throwable,
        executedArg: String,
        commandArg: Arg?,
    ) {
        // when passing an invocation target exception, we first have to extract the actual exception that occurred.
        val exception = originalException.extractNonInvocationTargetException()
        val exceptionHandlers = argExceptionHandlers[commandArg] ?: emptyMap()
        // we may or may not have an exception handler mapped for this execution context
        val context =
            exceptionHandlers.entries
                .filter { it.key.isAssignableFrom(exception.javaClass) }
                .map { it.value }
                .firstOrNull()
        if (context == null || commandArg == null) {
            // we do not have any exception handler mapped for this argument, or the passed argument is null
            // try to find a global exception handler
            return executeGlobalExceptionHandlerMethod(sender, executedArg, commandArg, exception)
        }

        try {
            context.handlerMethod(
                this,
                *context.getInjectableArgExceptionHandlerMethodParameters(sender, executedArg, commandArg, exception),
            )
        } catch (e: Exception) {
            try {
                // an exception occurred while invoking the arg exception handler, try to find a global one instead
                executeGlobalExceptionHandlerMethod(sender, executedArg, commandArg, e)
            } catch (e: Exception) {
                // finally, if this also fails, we throw our own exception to signalize terminal failure
                throw VitalCommandException.ExecuteArgExceptionHandlerMethod(context.handlerMethod, context, e)
            }
        }
    }

    /**
     * Executes the handler method associated with a specific command argument.
     */
    private fun executeArgHandlerMethod(
        sender: CS,
        executedArg: String,
        commandArg: Arg,
        values: Array<String>,
    ): ReturnState {
        val context = argHandlers[commandArg] ?: throw VitalCommandException.UnmappedArgHandler(executedArg)
        return context.handlerMethod(
            this,
            *context.getInjectableArgHandlerMethodParameters(sender, executedArg, commandArg, values),
        ) as ReturnState
    }

    /**
     * Provides tab completion suggestions for commands based on the input arguments and context.
     */
    fun tabComplete(
        sender: CS,
        args: Array<String>,
    ): List<String> {
        val tabCompleted = mutableListOf<String>()
        for ((_, commandArg) in this.args) {
            val splitCommandArg = commandArg.name.split(" ")
            // the player has entered more arguments than the command arg supports, we can never have a hit here
            if (args.size > splitCommandArg.size) continue
            var commandArgMatches = true
            for ((i, enteredArg) in args.withIndex()) {
                if (i > 0) {
                    // we have previous entered arguments, check if they match with the command arg, so we know we are on the right node
                    for (prevI in 0..<i) {
                        val previousEnteredArg = args[prevI]
                        val previousCommandArg =
                            splitCommandArg[prevI]
                                .replace(VARARG_REGEX.toRegex(), previousEnteredArg)
                                .replace(ARG_REGEX.toRegex(), previousEnteredArg)
                        if (previousCommandArg != previousEnteredArg) {
                            // our previously entered arg does not match with the registered command arg
                            commandArgMatches = false
                            break
                        }
                    }

                    if (!commandArgMatches) {
                        // any previous token does not match, we can stop right here
                        break
                    }
                }
                val matchableCommandArg =
                    splitCommandArg[i]
                        .replace(VARARG_REGEX.toRegex(), enteredArg)
                        .replace(ARG_REGEX.toRegex(), enteredArg)

                if (!matchableCommandArg.startsWith(enteredArg)) {
                    commandArgMatches = false
                    break
                }

                commandArgMatches = true
            }

            if (commandArgMatches) {
                tabCompleted.add(splitCommandArg.subList(args.size - 1, splitCommandArg.size).joinToString(" "))
                // only the last element should be converted to argument type check to avoid confusing tab completions
                val argType = Arg.Type.getTypeByPlaceholder(splitCommandArg[args.size - 1])
                argType?.action?.invoke(TabCompletionContext(tabCompleted, getAllPlayerNames()))
                tabCompleted.addAll(
                    onCommandTabComplete(
                        sender,
                        splitCommandArg.subList(0, args.size).joinToString(" "),
                    ),
                )
            }
        }

        return tabCompleted
    }

    /**
     * Executes a command with the given sender and arguments.
     */
    fun execute(
        sender: CS,
        args: Array<String>,
    ) {
        val joinedPlayerArgs = args.joinToString(" ")
        if (playerOnly && !isPlayer(sender)) return onCommandRequiresPlayer(sender, joinedPlayerArgs, null)

        val matchedArg = getArg(joinedPlayerArgs)
        val returnState =
            when {
                permission.isNotBlank() && !hasPermission(sender, permission) -> ReturnState.NO_PERMISSION

                matchedArg != null -> {
                    if (matchedArg.permission.isNotBlank() && !hasPermission(sender, matchedArg.permission)) {
                        ReturnState.NO_PERMISSION
                    } else if (matchedArg.playerOnly && !isPlayer(sender)) {
                        ReturnState.ONLY_PLAYER
                    } else {
                        val values = mutableListOf<String>()
                        val commandArgs =
                            matchedArg.name
                                .split(" ".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .map { it.lowercase() }

                        args
                            .map(String::lowercase)
                            .filter {
                                it !in commandArgs ||
                                    (
                                        (it.startsWith("%") && it.endsWith("%")) ||
                                            (
                                                it.startsWith("<") &&
                                                    it.endsWith(
                                                        ">",
                                                    )
                                            )
                                    )
                            }.forEach(values::add)

                        try {
                            executeArgHandlerMethod(sender, joinedPlayerArgs, matchedArg, values.toTypedArray())
                        } catch (e: Exception) {
                            executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, matchedArg)
                        }
                    }
                }

                else -> ReturnState.INVALID_ARGS
            }

        try {
            when (returnState) {
                ReturnState.SUCCESS -> {}
                ReturnState.INVALID_ARGS -> onCommandInvalidArgs(sender, joinedPlayerArgs)
                ReturnState.NO_PERMISSION -> onCommandRequiresPermission(sender, joinedPlayerArgs, matchedArg)
                ReturnState.ONLY_PLAYER -> onCommandRequiresPlayer(sender, joinedPlayerArgs, matchedArg)
            }
        } catch (e: Exception) {
            executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, matchedArg)
        }
    }

    /**
     * Called as a fallback, when all other command exception handling mechanism fail (or are non-existent).
     */
    protected open fun onCommandError(
        sender: CS,
        commandArg: Arg?,
        e: Throwable,
    ): Unit = throw e

    /**
     * Called when the tab completer is ready to add more tab completions to the already existing ones provided by Vital.
     * You can use this function to add more tab completions.
     */
    protected open fun onCommandTabComplete(
        sender: CS,
        args: String,
    ) = listOf<String>()

    /**
     * Called when an argument is called with invalid or missing arguments.
     */
    protected open fun onCommandInvalidArgs(
        sender: CS,
        args: String,
    ) {
    }

    /**
     * Called when an argument is called without the required permissions.
     */
    protected open fun onCommandRequiresPermission(
        sender: CS,
        args: String,
        commandArg: Arg?,
    ) {
    }

    /**
     * Called when a player-only argument is called by a non-player.
     */
    protected open fun onCommandRequiresPlayer(
        sender: CS,
        args: String,
        commandArg: Arg?,
    ) {
    }

    /**
     * Defines the possible [VitalCommand] return states of a [VitalCommand.ArgHandler] annotated function.
     */
    enum class ReturnState {
        INVALID_ARGS,
        SUCCESS,
        NO_PERMISSION,
        ONLY_PLAYER,
    }

    /**
     * Defines the info for a [VitalCommand].
     */
    @Component
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Info(
        val name: String,
        val description: String,
        val aliases: Array<String> = [],
        val usage: String = "",
        val permission: String = "",
        val playerOnly: Boolean = true,
    )

    /**
     * Defines the argument of a [VitalCommand.ArgHandler] annotated function.
     */
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Arg(
        val name: String = "",
        val permission: String = "",
        val playerOnly: Boolean = false,
    ) {
        enum class Type(
            val placeholders: List<String>,
            val action: (TabCompletionContext) -> Unit,
        ) {
            // TODO: later remove any %...% placeholder
            // TODO: future placeholders should always be <...>
            PLAYER(
                listOf("%PLAYER%", "<player>"),
                { context ->
                    context.playerNames.filter { it !in context.completions }.forEach { context.completions.add(it) }
                },
            ),
            BOOLEAN(listOf("%BOOLEAN%", "<boolean>"), { it.completions.addAll(arrayOf("true", "false")) }),
            NUMBER(listOf("%NUMBER%", "<number>"), { it.completions.add("0") }),
            MATERIAL(listOf("%MATERIAL%", "<material>"), { context ->
                Material.entries.map { it.name }.forEach { context.completions.add(it) }
            }),
            ;

            companion object {
                @JvmStatic
                fun getTypeByPlaceholder(placeholder: String) =
                    entries.firstOrNull {
                        placeholder.lowercase() in
                            it.placeholders.map { it.lowercase() }
                    }
            }
        }
    }

    /**
     * Marks the annotated function as a valid argument entry-point within a [VitalCommand].
     */
    @Repeatable
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class ArgHandler(
        val arg: Arg = Arg(),
    )

    /**
     * Marks the annotated function as a valid entry-point for when exception occur during the execution of a [VitalCommand.ArgHandler] annotated function.
     */
    @Repeatable
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ArgExceptionHandler(
        val arg: String,
        val type: KClass<out Throwable>,
    )

    /**
     * Marks the annotated class to be used as a global command exception handler.
     * This class may define multiple [VitalCommand.GlobalExceptionHandler] annotated functions to handle exceptions during command execution.
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Advice(
        val commandSenderClass: KClass<out Any>,
    )

    /**
     * Marks the annotated function as a global exception handler.
     * This function will be called when an exception is thrown within the execution of an [VitalCommand.ArgHandler] annotated function,
     * and is not caught by an underlying [VitalCommand.ArgExceptionHandler] annotated function.
     *
     * Use this if you want to have a fallback for error messages, or if your want to reduce boilerplate.
     */
    @Repeatable
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class GlobalExceptionHandler(
        val type: KClass<out Throwable>,
    )

    /**
     * Defines the context required to execute a [VitalCommand.GlobalExceptionHandler] annotated function.
     */
    data class GlobalExceptionHandlerContext(
        val adviceInstance: Any,
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
        val exceptionIndex: Int?,
    )

    /**
     * Defines the context required to execute tab completion on a [VitalCommand].
     */
    data class TabCompletionContext(
        val completions: MutableList<String>,
        val playerNames: List<String>,
    )

    /**
     * Defines the context required to execute a [VitalCommand.ArgHandler] annotated function.
     */
    data class ArgHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
    )

    /**
     * Defines the context required to execute a [VitalCommand.ArgExceptionHandler] annotated function.
     */
    data class ArgExceptionHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val exceptionIndex: Int?,
    )

    abstract class Spigot :
        VitalCommand<SpigotCommandSender>(SpigotCommandSender::class.java),
        VitalPluginCommand.Spigot {
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
        ) = tabComplete(sender, args)

        override fun isPlayer(commandSender: SpigotCommandSender) = commandSender is SpigotPlayer

        override fun hasPermission(
            commandSender: SpigotCommandSender,
            permission: String,
        ) = commandSender.hasPermission(permission)

        override fun getAllPlayerNames() = Bukkit.getOnlinePlayers().map { it.name }
    }

    abstract class Bungee : VitalCommand<BungeeCommandSender>(BungeeCommandSender::class.java) {
        override fun isPlayer(commandSender: BungeeCommandSender) = commandSender is BungeePlayer

        override fun hasPermission(
            commandSender: BungeeCommandSender,
            permission: String,
        ) = commandSender.hasPermission(permission)

        override fun getAllPlayerNames() = ProxyServer.getInstance().players.map { it.name }
    }

    companion object {
        /**
         * Regex used to determine the splitting character for all [VitalCommand.Arg]s.
         */
        const val SPACE_REGEX = " "

        /**
         * Regex used to determine the dynamic varargs within a [VitalCommand.Arg].
         */
        const val VARARG_REGEX = "(%\\S*%\\*|<\\S*>\\*)"

        /**
         * Regex used to determine the dynamic args within a [VitalCommand.Arg].
         */
        const val ARG_REGEX = "(%\\S*%(?!\\*)|<\\S*>(?!\\*))"

        /**
         * The replacement for all sequences matching [SPACE_REGEX].
         */
        const val SPACE_REPLACEMENT = " "

        /**
         * The replacement for all sequences matching [VARARG_REGEX].
         */
        const val VARARG_REPLACEMENT = ".+"

        /**
         * The replacement for all sequences matching [ARG_REGEX].
         */
        const val ARG_REPLACEMENT = "\\\\S+"

        /**
         * Retrieves a map of argument patterns to their corresponding argument definitions
         * for a `VitalCommand`. It processes the methods of the `VitalCommand` instance
         * to look for annotated argument handlers and maps argument patterns to their metadata.
         *
         * The method performs the following steps:
         * - Filters methods annotated with `@ArgHandler`.
         * - Extracts all `ArgHandler` annotations from these methods.
         * - Processes the `name` property of each argument by replacing placeholders based on
         *   specific regex patterns (`SPACE_REGEX`, `VARARG_REGEX`, `ARG_REGEX`) and their
         *   defined replacements (`SPACE_REPLACEMENT`, `VARARG_REPLACEMENT`, `ARG_REPLACEMENT`).
         * - Compiles the processed `name` as a regex pattern and associates it with the argument definition.
         *
         * @return A map where each key is a compiled regular expression pattern representing the argument name,
         * and each value is the corresponding argument definition.
         */
        @JvmStatic
        fun VitalCommand<*>.getMappedArgs() =
            javaClass.methods
                .filter { it.getAnnotationsByType(ArgHandler::class.java).size > 0 }
                .map { it.getAnnotationsByType(ArgHandler::class.java).toList() }
                .flatten()
                .associate {
                    Pattern.compile(
                        it.arg.name
                            .replace(SPACE_REGEX.toRegex(), SPACE_REPLACEMENT)
                            .replace(VARARG_REGEX.toRegex(), VARARG_REPLACEMENT)
                            .replace(ARG_REGEX.toRegex(), ARG_REPLACEMENT),
                    ) to it.arg
                }

        /**
         * Retrieves a mapping of argument handlers annotated within the `VitalCommand` class.
         *
         * This method scans the methods of the current `VitalCommand` instance for those that are annotated
         * with `@ArgHandler`. Each identified method is paired with its corresponding annotation and processed
         * to create a map where the key is the argument name declared in the `@ArgHandler` annotation, and
         * the value is the handler context for that specific argument.
         *
         * The handler context includes preparation for allowed injectable parameters, ensuring compatibility
         * with the framework's supported argument-handling structure.
         *
         * @return A map where each key represents the argument name defined in the `@ArgHandler` annotation,
         * and each value represents the prepared handler context for the argument's processing.
         */
        @JvmStatic
        fun VitalCommand<*>.getMappedArgHandlers() =
            javaClass.methods
                .asSequence()
                .filter { it.getAnnotationsByType(ArgHandler::class.java).size > 0 }
                .map { method -> method.getAnnotationsByType(ArgHandler::class.java).map { method to it } }
                .flatten()
                .associate { (method, argHandler) ->
                    // now we have a viable method ready for handling incoming arguments
                    // we just need to filter out the injectable parameters for our method
                    // since we only support a handful of injectable params for handler methods...
                    argHandler.arg to method.getArgHandlerContext(commandSenderClass)
                }

        /**
         * Retrieves an array of parameters to be injected into a handler method
         * based on the provided context and input parameters.
         *
         * @param context Provides contextual information such as indexes for the injectable parameters.
         * @param sender The sender associated with the command execution.
         * @param executedArg The argument string that was executed.
         * @param commandArg The command argument object associated with the executed argument.
         * @param values The array of values that were supplied for the command arguments.
         * @return An array of parameters to be injected, ordered by their respective indexes.
         */
        @JvmStatic
        fun ArgHandlerContext.getInjectableArgHandlerMethodParameters(
            sender: Any,
            executedArg: String,
            commandArg: Arg,
            values: Array<String>,
        ): Array<Any> {
            val injectableParameters = mutableMapOf<Int, Any>()

            commandSenderIndex?.let { injectableParameters[it] = sender }
            executedArgIndex?.let { injectableParameters[it] = executedArg }
            commandArgIndex?.let { injectableParameters[it] = commandArg }
            valuesIndex?.let { injectableParameters[it] = values }

            return injectableParameters.entries
                .sortedBy { it.key }
                .map { it.value }
                .toTypedArray()
        }

        /**
         * Retrieves a mutable map of mapped argument exception handlers for the current command.
         * The method scans for methods annotated with `@VitalCommand.ArgExceptionHandler`
         * and organizes them by their associated arguments and exception types.
         *
         * @return A mutable map where the keys are the command arguments (`VitalCommand.Arg`)
         * and the values are nested mutable maps. The nested maps have exception
         * types (`Class<out Throwable>`) as keys and exception handler context (`VitalCommand.ArgExceptionHandlerContext`) as values.
         */
        @JvmStatic
        fun VitalCommand<*>.getMappedArgExceptionHandlers(): MutableMap<
            Arg,
            MutableMap<Class<out Throwable>, ArgExceptionHandlerContext>,
        > {
            val mappedArgExceptionHandlers =
                mutableMapOf<Arg, MutableMap<Class<out Throwable>, ArgExceptionHandlerContext>>()

            javaClass.methods
                .filter { it.getAnnotationsByType(ArgExceptionHandler::class.java).size > 0 }
                .map { method -> method.getAnnotationsByType(ArgExceptionHandler::class.java).map { method to it } }
                .flatten()
                .forEach { (method, argExceptionHandler) ->
                    val arg =
                        getArg(argExceptionHandler.arg)
                            ?: throw VitalCommandException.UnmappedArgExceptionHandlerArg(
                                method,
                                argExceptionHandler.arg,
                            )
                    val context = method.getArgExceptionHandlerContext(commandSenderClass)

                    if (!mappedArgExceptionHandlers.containsKey(arg)) {
                        mappedArgExceptionHandlers[arg] = mutableMapOf(argExceptionHandler.type.java to context)
                    } else {
                        mappedArgExceptionHandlers[arg]!![argExceptionHandler.type.java] = context
                    }
                }

            return mappedArgExceptionHandlers
        }

        /**
         * Constructs an array of method parameters to inject into an exception handler method
         * based on the provided context and arguments.
         *
         * @param context The context containing metadata about the exception handler method,
         * including parameter indices for injectable arguments.
         * @param sender The sender object, typically representing the entity or object that triggered the command.
         * @param executedArg The argument string that was executed in the command causing the exception.
         * @param commandArg The specific command argument associated with the executed argument.
         * @param exception The exception that occurred during execution.
         * @return An array of objects representing the parameters to be injected into the exception handler
         * method, ordered by their parameter indices.
         */
        @JvmStatic
        fun ArgExceptionHandlerContext.getInjectableArgExceptionHandlerMethodParameters(
            sender: Any,
            executedArg: String,
            commandArg: Arg,
            exception: Throwable,
        ): Array<Any> {
            val injectableParameters = mutableMapOf<Int, Any>()

            commandSenderIndex?.let { injectableParameters[it] = sender }
            executedArgIndex?.let { injectableParameters[it] = executedArg }
            commandArgIndex?.let { injectableParameters[it] = commandArg }
            exceptionIndex?.let { injectableParameters[it] = exception }

            return injectableParameters.entries
                .sortedBy { it.key }
                .map { it.value }
                .toTypedArray()
        }

        /**
         * Constructs an `ArgHandlerContext` for the given method of a command sender class.
         *
         * @receiver The method to analyze and build the `ArgHandlerContext`.
         * @param commandSenderClass The class type of the command sender.
         * @return An `ArgHandlerContext` object that contains parsed parameter indexes for
         * handling command arguments.
         * @throws VitalCommandException.InvalidArgHandlerReturnSignature If the method does not return `VitalCommand.ReturnState`.
         * @throws VitalCommandException.InvalidArgHandlerParameterSignature If any method parameter has an invalid type.
         */
        @JvmStatic
        fun Method.getArgHandlerContext(commandSenderClass: Class<*>): ArgHandlerContext {
            if (returnType != ReturnState::class.java) {
                throw VitalCommandException.InvalidArgHandlerReturnSignature(this, returnType)
            }
            var commandSenderIndex: Int? = null
            var executedArgIndex: Int? = null
            var commandArgIndex: Int? = null
            var valuesIndex: Int? = null

            for (parameter in parameters) {
                when {
                    commandSenderClass.isAssignableFrom(parameter.type) ->
                        commandSenderIndex =
                            parameters.indexOf(parameter)

                    String::class.java.isAssignableFrom(parameter.type) ->
                        executedArgIndex =
                            parameters.indexOf(parameter)

                    Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex = parameters.indexOf(parameter)
                    Array<String>::class.java.isAssignableFrom(parameter.type) ->
                        valuesIndex =
                            parameters.indexOf(parameter)

                    else -> throw VitalCommandException.InvalidArgHandlerParameterSignature(this, parameter)
                }
            }

            return ArgHandlerContext(this, commandSenderIndex, executedArgIndex, commandArgIndex, valuesIndex)
        }

        /**
         * Constructs an `ArgExceptionHandlerContext` for a given method and command sender class. It identifies the indices
         * of necessary parameters such as the command sender, executed argument, command argument, and exception within the
         * provided method's parameter list.
         *
         * @receiver The method whose parameters are analyzed to retrieve the context.
         * @param commandSenderClass The class of the command sender used to identify the corresponding parameter.
         * @return An instance of `VitalCommand.ArgExceptionHandlerContext` containing the indices of relevant parameters
         *         within the method's parameters.
         * @throws VitalCommandException.InvalidArgExceptionHandlerMethodSignature If a parameter of an invalid type is
         *         encountered in the method.
         */
        @JvmStatic
        fun Method.getArgExceptionHandlerContext(commandSenderClass: Class<*>): ArgExceptionHandlerContext {
            var commandSenderIndex: Int? = null
            var executedArgIndex: Int? = null
            var commandArgIndex: Int? = null
            var exceptionIndex: Int? = null

            for (parameter in parameters) {
                when {
                    commandSenderClass.isAssignableFrom(parameter.type) ->
                        commandSenderIndex =
                            parameters.indexOf(parameter)

                    String::class.java.isAssignableFrom(parameter.type) ->
                        executedArgIndex =
                            parameters.indexOf(parameter)

                    Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex = parameters.indexOf(parameter)
                    Exception::class.java.isAssignableFrom(parameter.type) ->
                        exceptionIndex =
                            parameters.indexOf(parameter)

                    else -> throw VitalCommandException.InvalidArgExceptionHandlerMethodSignature(this, parameter)
                }
            }

            return ArgExceptionHandlerContext(
                this,
                commandSenderIndex,
                executedArgIndex,
                commandArgIndex,
                exceptionIndex,
            )
        }

        /**
         * Constructs and returns a `GlobalExceptionHandlerContext` instance for managing a global exception handler.
         *
         * @receiver The method to be analyzed for parameter indices and validated for its eligibility as a global exception handler.
         * @param adviceInstance An instance of the advice that contains the exception handler method.
         * @param commandSenderClass The class type representing the command sender in the context of the global exception handler.
         * @return A `GlobalExceptionHandlerContext` containing metadata about the exception handler such as parameter indices.
         * @throws VitalCommandException.InvalidGlobalExceptionHandlerMethodSignature If the method signature has unsupported parameter types.
         */
        @JvmStatic
        fun Method.getGlobalExceptionHandlerContext(
            adviceInstance: Any,
            commandSenderClass: Class<*>,
        ): GlobalExceptionHandlerContext {
            var commandSenderIndex: Int? = null
            var executedArgIndex: Int? = null
            var commandArgIndex: Int? = null
            var valuesIndex: Int? = null
            var exceptionIndex: Int? = null

            for (parameter in parameters) {
                when {
                    commandSenderClass.isAssignableFrom(parameter.type) ->
                        commandSenderIndex =
                            parameters.indexOf(parameter)

                    String::class.java.isAssignableFrom(parameter.type) ->
                        executedArgIndex =
                            parameters.indexOf(parameter)

                    Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex = parameters.indexOf(parameter)
                    Array<String>::class.java.isAssignableFrom(parameter.type) ->
                        valuesIndex =
                            parameters.indexOf(parameter)

                    Throwable::class.java.isAssignableFrom(parameter.type) ->
                        exceptionIndex =
                            parameters.indexOf(parameter)

                    else -> throw VitalCommandException.InvalidGlobalExceptionHandlerMethodSignature(this, parameter)
                }
            }

            return GlobalExceptionHandlerContext(
                adviceInstance,
                this,
                commandSenderIndex,
                executedArgIndex,
                commandArgIndex,
                valuesIndex,
                exceptionIndex,
            )
        }

        /**
         * Constructs an array of parameters to be injected into a global exception handler method
         * based on the specified indices in the provided context.
         *
         * @param context The context containing index mappings for injectable parameters.
         * @param sender The command sender instance to be injected if applicable.
         * @param executedArg The executed argument to be injected if applicable.
         * @param commandArg The optional command argument to be injected if applicable.
         * @param exception The exception thrown during command execution to be injected if applicable.
         * @return An array of parameters to be injected into the global exception handler method.
         */
        @JvmStatic
        fun GlobalExceptionHandlerContext.getInjectableGlobalExceptionHandlerMethodParameters(
            sender: Any,
            executedArg: String,
            commandArg: Arg?,
            exception: Throwable,
        ): Array<Any?> {
            val injectableParameters = mutableMapOf<Int, Any?>()

            commandSenderIndex?.let { injectableParameters[it] = sender }
            executedArgIndex?.let { injectableParameters[it] = executedArg }
            commandArgIndex?.let { injectableParameters[it] = commandArg }
            exceptionIndex?.let { injectableParameters[it] = exception }

            return injectableParameters.entries
                .sortedBy { it.key }
                .map { it.value }
                .toTypedArray()
        }

        /**
         * Retrieves the VitalCommand.Info annotation associated with this class.
         *
         * @receiver the class of the VitalCommand instance for which the annotation is to be retrieved.
         * @return the VitalCommand.Info annotation of this class
         */
        @JvmStatic
        fun Class<out VitalCommand<*>>.getInfo(): Info = getRequiredAnnotation<Info>()

        /**
         * Retrieves the VitalCommand.Info annotation associated with this class.
         *
         * @receiver the class of the VitalCommand instance for which the annotation is to be retrieved.
         * @return the VitalCommand.Info annotation of this class.
         */
        @JvmStatic
        fun KClass<out VitalCommand<*>>.getInfo(): Info = java.getInfo()

        /**
         * Retrieves the VitalCommand.Info annotation associated with this instance.
         *
         * @receiver the VitalCommand instance for which the annotation is to be retrieved.
         * @return the VitalCommand.Info annotation of this instance.
         */
        @JvmStatic
        fun VitalCommand<*>.getInfo(): Info = javaClass.getInfo()

        /**
         * Retrieves the VitalCommand.Advice annotation associated with this class.
         *
         * @receiver the class for which the annotation is to be retrieved.
         * @return the VitalCommand.Advice annotation of this class.
         */
        @JvmStatic
        fun Class<*>.getVitalCommandAdvice(): Advice = getRequiredAnnotation<Advice>()

        /**
         * Retrieves the VitalCommand.Advice annotation associated with this class.
         *
         * @receiver the class for which the annotation is to be retrieved.
         * @return the VitalCommand.Advice annotation of this class.
         */
        @JvmStatic
        fun KClass<*>.getVitalCommandAdvice(): Advice = java.getVitalCommandAdvice()
    }
}
