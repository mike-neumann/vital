package dev.vitalframework.commands

import dev.vitalframework.BungeeCommandSender
import dev.vitalframework.BungeePlayer
import dev.vitalframework.SpigotCommandSender
import dev.vitalframework.SpigotPlayer
import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalHasInfo
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * Use this class to define your own custom command.
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
) : VitalHasInfo {
    override val info = mutableMapOf(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    private val logger = logger()

    /**
     * The arguments of the command, mapped from [Pattern] to [VitalCommand.Arg].
     */
    val args = getMappedArgHandlers()

    /**
     * The [VitalCommand.ArgHandlerContext] mapped by their [VitalCommand.Arg].
     */
    val argHandlers = getMappedArgHandlerContext()

    /**
     * The [Throwable] (type of exception) and their [VitalCommand.ArgExceptionHandlerContext] mapped by their [VitalCommand.Arg].
     */
    val argExceptionHandlers = getMappedArgExceptionHandlers()

    /**
     * Internal function used to check if the given [commandSender] is a player.
     */
    abstract fun isPlayer(commandSender: CS): Boolean

    /**
     * Internal function used to check if the given [commandSender] has the given [permission].
     */
    abstract fun hasPermission(
        commandSender: CS,
        permission: String,
    ): Boolean

    /**
     * Internal function used to get the names of all currently connected players.
     */
    abstract fun getAllPlayerNames(): List<String>

    /**
     * Internal function used to get the [Arg] that matches with the given [executedArg].
     */
    internal fun getArg(executedArg: String) =
        args.entries
            .filter { it.key.matcher(executedArg).matches() }
            .map { it.value }
            .firstOrNull()

    /**
     * Internal function used to execute all registered global exception handlers for the given command execution.
     * If the execution fails for any reason, [onCommandError] will be called as a fallback.
     */
    private fun executeGlobalExceptionHandlerMethod(
        sender: CS,
        executedArg: String,
        commandArg: Arg?,
        originalException: Throwable,
    ) {
        val loggingContext =
            "Context: Command '${javaClass.simpleName}', sender '$sender', " +
                "executed command '$executedArg', found command arg '$commandArg', exception '$originalException'."
        logger.debug("Attempting to find and execute global exception handler. $loggingContext")

        val exception = originalException.extractNonInvocationTargetException()
        logger.debug("Extracted exception '$exception' for global exception handler execution. $loggingContext")

        val globalContext =
            VitalGlobalCommandExceptionHandlerProcessor.getGlobalExceptionHandler(exception.javaClass)
                ?: try {
                    logger.debug("Could not find global exception handler, will execute onCommandError. $loggingContext")
                    return onCommandError(sender, commandArg, exception)
                } catch (e: Exception) {
                    logger.debug(
                        "Exception while calling onCommandError, will try to find a global exception handler for exception '$e'. $loggingContext",
                    )
                    // try to find a global exception handler mapped for the exception that was thrown while handling a command error
                    val secondGlobalContext =
                        VitalGlobalCommandExceptionHandlerProcessor.getGlobalExceptionHandler(e.javaClass)
                    if (secondGlobalContext == null) {
                        logger.debug("No global exception handler found for exception '$e' while executing onCommandError. $loggingContext")
                        // if we couldn't find a global exception handler for this exception, we are out of options...
                        // simply rethrow this exception so it is redirected to default exception handling...
                        throw e
                    } else {
                        logger.debug(
                            "Global exception handler '${secondGlobalContext.handlerMethod.name}' found for exception '$e' while executing onCommandError. $loggingContext",
                        )
                    }

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
            logger.debug("Global exception handler '${globalContext.handlerMethod.name}' found. $loggingContext")
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
     * Internal function used to execute all arg exception handlers for the given command execution.
     * If the execution fails for any reason, the exception will be delegated to global exceptions handlers by calling [executeGlobalExceptionHandlerMethod].
     */
    private fun executeArgExceptionHandlerMethod(
        sender: CS,
        originalException: Throwable,
        executedArg: String,
        commandArg: Arg?,
    ) {
        val loggingContext =
            "Context: Command: ${javaClass.simpleName}, sender '$sender', " +
                "executed command '$executedArg', found command arg '$commandArg'."
        logger.debug("Attempting to find and execute arg exception handler. $loggingContext")

        // when passing an invocation target exception, we first have to extract the actual exception that occurred.
        val exception = originalException.extractNonInvocationTargetException()
        logger.debug("Extracted exception '$exception' for arg exception handler execution. $loggingContext")

        val exceptionHandlers = argExceptionHandlers[commandArg] ?: emptyMap()
        logger.debug("Viable arg exception handlers are '$exceptionHandlers'. $loggingContext")

        // we may or may not have an exception handler mapped for this execution context
        val context =
            exceptionHandlers.entries
                .filter { it.key.isAssignableFrom(exception.javaClass) }
                .map { it.value }
                .firstOrNull()
        logger.debug("Viable arg exception handler for this context '$context'. $loggingContext")

        if (context == null || commandArg == null) {
            // we do not have any exception handler mapped for this argument, or the passed argument is null
            // try to find a global exception handler
            logger.debug(
                "No viable arg exception handler found for this context. Will try to find and execute a global exception handler. $loggingContext",
            )
            return executeGlobalExceptionHandlerMethod(sender, executedArg, commandArg, exception)
        }

        try {
            logger.debug("Arg exception handler found '${context.handlerMethod.name}', trying to call it. $loggingContext")
            context.handlerMethod(
                this,
                *context.getInjectableArgExceptionHandlerMethodParameters(sender, executedArg, commandArg, exception),
            )
        } catch (e: Exception) {
            try {
                logger.debug(
                    "Error while trying to call arg exception handler '${context.handlerMethod.name}'. Will try to find a global exception handler for the new exception '$e'. $loggingContext",
                )
                // an exception occurred while invoking the arg exception handler, try to find a global one instead
                executeGlobalExceptionHandlerMethod(sender, executedArg, commandArg, e)
            } catch (e: Exception) {
                // finally, if this also fails, we throw our own exception to signalize terminal failure
                throw VitalCommandException.ExecuteArgExceptionHandlerMethod(context.handlerMethod, context, e)
            }
        }
    }

    /**
     * Internal function used to execute the arg handler for the given command execution.
     * If the execution fails for any reason, any known arg exception handlers will be called using [executeArgExceptionHandlerMethod].
     */
    private fun executeArgHandlerMethod(
        sender: CS,
        executedArg: String,
        commandArg: Arg,
        values: Array<String>,
    ): ReturnState {
        val loggingContext =
            "Context: Command '${javaClass.simpleName}', sender '$sender', " +
                "executedArg '$executedArg', command arg '$commandArg', values '${values.contentToString()}'."
        try {
            logger.debug("Attempting to find and execute arg handler method. $loggingContext")
            val context = argHandlers[commandArg] ?: throw VitalCommandException.UnmappedArgHandler(executedArg)
            logger.debug("Arg handler method found '${context.handlerMethod.name}'. $loggingContext")
            return context.handlerMethod(
                this,
                *context.getInjectableArgHandlerMethodParameters(sender, executedArg, commandArg, values),
            ) as ReturnState
        } catch (e: Exception) {
            logger.debug(
                "Error while trying to call arg handler method. Will try to find an arg exception handler for exception '$e'. $loggingContext",
            )
            executeArgExceptionHandlerMethod(sender, e, executedArg, commandArg)
            return ReturnState.SUCCESS
        }
    }

    /**
     * Internal function used to provide automatic tab-completions for the given command execution.
     * Additionally, implementers can add their own tab-completions by overriding the [onCommandTabComplete] lifecycle function.
     */
    fun tabComplete(
        sender: CS,
        args: Array<String>,
    ): List<String> {
        val logginContext = "Context: Command '${javaClass.simpleName}', sender '$sender', args '${args.contentToString()}'."
        logger.debug("Requested tab complete. $logginContext")
        val tabCompleted = mutableListOf<String>()
        for ((_, commandArg) in this.args) {
            logger.debug("Stepping args for tab completion. Current arg '${commandArg.name}'. $logginContext")
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
                logger.debug(
                    "Arg '${commandArg.name}' matches for tab completion. Also adding all tab completions from onCommandTabComplete. $logginContext",
                )
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

        logger.debug("Will display the following '${tabCompleted.size}' tab completions: '$tabCompleted'.")
        return tabCompleted
    }

    /**
     * Internal function used to execute this command as the given [sender] and [args].
     * Any mapped arg handlers that matches the given command execution will be called using [executeArgExceptionHandlerMethod].
     */
    fun execute(
        sender: CS,
        args: Array<String>,
    ) {
        val logginContext = "Context: Command '${javaClass.simpleName}', sender '$sender', args '${args.contentToString()}'."
        logger.debug("Attempting to execute command. $logginContext")
        val info = getInfo(Info::class.java)
        val joinedPlayerArgs = args.joinToString(" ")
        if (info.playerOnly && !isPlayer(sender)) {
            logger.debug(
                "Sender is not a player, but this command can only be executed by a player. Will call onCommandRequiresPlayer. $logginContext",
            )
            return onCommandRequiresPlayer(sender, joinedPlayerArgs, null)
        }

        val matchedArg = getArg(joinedPlayerArgs)
        logger.debug("Command arg found '$matchedArg'. $logginContext")
        val returnState =
            when {
                info.permission.isNotBlank() && !hasPermission(sender, info.permission) -> {
                    logger.debug("Sender does not have the required permissions '${info.permission}' for this command. $logginContext")
                    ReturnState.NO_PERMISSION
                }

                matchedArg != null -> {
                    if (matchedArg.permission.isNotBlank() && !hasPermission(sender, matchedArg.permission)) {
                        logger.debug(
                            "Sender does not have the required permissions '${matchedArg.permission}' for this arg '$matchedArg'. $logginContext",
                        )
                        ReturnState.NO_PERMISSION
                    } else if (matchedArg.playerOnly && !isPlayer(sender)) {
                        logger.debug("Sender is not a player but this arg '$matchedArg' requires a player. $logginContext")
                        ReturnState.ONLY_PLAYER
                    } else {
                        logger.debug("Sender is permitted to execute this command and arg '$matchedArg'. $logginContext")
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

                        logger.debug(
                            "Values extracted from sender input '${args.contentToString()}' for arg '$matchedArg'. Will try to execute a mapped arg handler. $logginContext",
                        )
                        executeArgHandlerMethod(sender, joinedPlayerArgs, matchedArg, values.toTypedArray())
                    }
                }

                else -> {
                    logger.debug("Sender input is not valid for this command and arg '$matchedArg'. $logginContext")
                    ReturnState.INVALID_ARGS
                }
            }

        try {
            when (returnState) {
                ReturnState.SUCCESS -> {
                    logger.debug("Command execution successful. $logginContext")
                }
                ReturnState.INVALID_ARGS -> {
                    logger.debug("Sender has provided invalid args. Will call onCommandInvalidArg. $logginContext")
                    onCommandInvalidArgs(sender, joinedPlayerArgs)
                }
                ReturnState.NO_PERMISSION -> {
                    logger.debug("Sender does not have the required permission. Will call onCommandRequiresPermission. $logginContext")
                    onCommandRequiresPermission(sender, joinedPlayerArgs, matchedArg)
                }
                ReturnState.ONLY_PLAYER -> {
                    logger.debug(
                        "Command or arg requires a player while the sender is not a player. Will call onCommandRequiresPlayer. $logginContext",
                    )
                    onCommandRequiresPlayer(sender, joinedPlayerArgs, matchedArg)
                }
            }
        } catch (e: Exception) {
            logger.debug(
                "Error while processing command execution. Will try to find and execute an arg exception handler for exception '$e'. $logginContext",
            )
            executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, matchedArg)
        }
    }

    /**
     * Lifecycle function; called when an exception occurs during command execution.
     * This function is called as a last resort if no arg exception handlers and no global exception handlers were defined to handle the [e].
     */
    protected open fun onCommandError(
        sender: CS,
        commandArg: Arg?,
        e: Throwable,
    ) {
        logger.debug(
            "Lifecycle function 'onCommandError' was not overridden for command '${javaClass.simpleName}', will rethrow exception '$e' for sender '$sender' and arg '$commandArg' and possibly execute any global exception handlers for this exception.",
        )
        throw e
    }

    /**
     * Lifecycle function; called when a player types the command into chat and triggers tab-completions.
     * Use this function to add additional tab-completions if needed.
     *
     * The [args] are the same args used for your arg handlers, e.g. `add <player>`, `set <text>*`, etc.
     */
    protected open fun onCommandTabComplete(
        sender: CS,
        args: String,
    ): List<String> {
        logger.debug(
            "Lifecycle function 'onCommandTabComplete' was not overridden for command '${javaClass.simpleName}', this command will not provide any additional tab completions for sender '$sender' and args '$args'.",
        )
        return listOf()
    }

    /**
     * Lifecycle function; called when this command or any arg is executed with invalid args.
     */
    protected open fun onCommandInvalidArgs(
        sender: CS,
        args: String,
    ) {
        logger.debug(
            "Lifecycle function 'onCommandInvalidArgs' was not overridden for command '${javaClass.simpleName}'. Sender '$sender', args '$args'.",
        )
    }

    /**
     * Lifecycle function; called when this command or any arg is executed with insufficient permissions.
     */
    protected open fun onCommandRequiresPermission(
        sender: CS,
        args: String,
        commandArg: Arg?,
    ) {
        logger.debug(
            "Lifecycle function 'onCommandRequiresPermission' was not overridden for command '${javaClass.simpleName}'. Sender '$sender', args '$args', command arg '$commandArg'.",
        )
    }

    /**
     * Lifecycle function; called when this command or any arg is executed as a non-player but the command or arg requires a player.
     */
    protected open fun onCommandRequiresPlayer(
        sender: CS,
        args: String,
        commandArg: Arg?,
    ) {
        logger.debug(
            "Lifecycle function 'onCommandRequiresPlayer' was not overridden for command '${javaClass.simpleName}'. Sender '$sender', args '$args', command arg '$commandArg'.",
        )
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
     * Defines the metadata info for a [VitalCommand].
     */
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
         * Extracts the first non-[InvocationTargetException] from the given [Throwable].
         * This function will be used during exception handling to extract the actual exception that occurred.
         */
        fun Throwable.extractNonInvocationTargetException(): Throwable {
            var exception = this
            if (exception is InvocationTargetException) {
                var extractedException = targetException
                while (extractedException is InvocationTargetException) {
                    extractedException = extractedException.targetException
                }

                exception = extractedException
            }

            return exception
        }

        /**
         * Internal function used to Get all functions annotated with [ArgHandler] and map them to their given [Arg.name] as a [Pattern].
         * These mapped args are later used during command execution to find out which arg was actually executed by a command sender.
         */
        @JvmStatic
        fun VitalCommand<*>.getMappedArgHandlers() =
            javaClass.methods
                .filter { it.getAnnotationsByType(ArgHandler::class.java).size > 0 }
                .flatMap { it.getAnnotationsByType(ArgHandler::class.java).toList() }
                .associate {
                    Pattern.compile(
                        it.arg.name
                            .replace(SPACE_REGEX.toRegex(), SPACE_REPLACEMENT)
                            .replace(VARARG_REGEX.toRegex(), VARARG_REPLACEMENT)
                            .replace(ARG_REGEX.toRegex(), ARG_REPLACEMENT),
                    ) to it.arg
                }

        /**
         * Internal function used to get the internal arg handler context for all functions annotated with [ArgHandler].
         * This arg handler context will later be used during command execution to call the annotated arg handler function with the correct parameters.
         */
        @JvmStatic
        fun VitalCommand<*>.getMappedArgHandlerContext() =
            javaClass.methods
                .asSequence()
                .filter { it.getAnnotationsByType(ArgHandler::class.java).size > 0 }
                .flatMap { method -> method.getAnnotationsByType(ArgHandler::class.java).map { method to it } }
                .associate { (method, argHandler) ->
                    // now we have a viable method ready for handling incoming arguments
                    // we just need to filter out the injectable parameters for our method
                    // since we only support a handful of injectable params for handler methods...
                    argHandler.arg to method.getArgHandlerContext(commandSenderClass)
                }

        /**
         * Internal function to correctly sort all injectable function parameters for an arg handler and return them as an [Array],
         * so they can later be used to call their appropriate mapped arg handler function.
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
         * Internal function used to get all arg exception handler functions annotated with [ArgExceptionHandler] and map them to their appropriate [Arg] and specified [Annotation].
         * This function returns a [Map] of all mapped arg exception handlers and will later be used during command execution to correctly call arg exception handlers when an exception occurs during command execution.
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
                .flatMap { method -> method.getAnnotationsByType(ArgExceptionHandler::class.java).map { method to it } }
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
         * Internal function to correctly sort all injectable function parameters for an arg exception handler and return them as an [Array],
         * so they can later be used to call their appropriate mapped arg exception handler function.
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
         * Internal function used to get the [ArgHandlerContext] of the given method and [commandSenderClass].
         * The context will later be used during command execution to call the appropriate arg handler function.
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
         * Internal function used to get the [ArgExceptionHandlerContext] of the given method and [commandSenderClass].
         * The context will later be used during command execution to call the appropriate arg exception handler function.
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
         * Internal function used to get the [GlobalExceptionHandlerContext] of the given method, [adviceInstance] (The global exception handler class) and [commandSenderClass].
         * The context will later be used during command execution to call the appropriate global exception handler function.
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
         * Internal function to correctly sort all injectable function parameters for a global exception handler and return them as an [Array],
         * so they can later be used to call their appropriate mapped global exception handler function.
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
    }
}
