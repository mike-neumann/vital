package me.vitalframework.commands

import jakarta.annotation.PostConstruct
import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlayer
import me.vitalframework.BungeePlugin
import me.vitalframework.SpigotCommandSender
import me.vitalframework.SpigotPlayer
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalCoreSubModule.Companion.getRequiredAnnotation
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
 * Represents an abstract base class for defining a command in the system.
 * The `VitalCommand` class provides a framework for creating commands, including argument handling,
 * permissions, player-only checks, exception handling, and tab completion.
 *
 * The `VitalCommand` is generic and is parameterized by `P` for the plugin and `CS` for the command sender class.
 *
 * @param P The type of the plugin this command is a part of.
 * @param CS The type of the command sender expected for this command.
 * @param plugin The plugin instance associated with this command.
 * @param commandSenderClass The class representing the command sender.
 *
 * This class includes abstract methods that subclasses must implement to customize
 * command behaviors, such as determining if the sender is a player or checking permissions.
 *
 * Key features and lifecycle:
 * - Commands can handle arguments through custom mappings and their associated handlers.
 * - Exception handling is provided for both argument-level and global-level exceptions.
 * - The command execution process involves validating sender permissions and player-only constraints,
 *   resolving arguments, and invoking the appropriate handler methods.
 * - The tab completion functionality generates dynamic command suggestions based on the current input.
 *
 * Command execution progresses as follows:
 * 1. Validates that the sender satisfies player-only and permission constraints.
 * 2. Retrieves and validates the arguments from the command input.
 * 3. Executes the appropriate handlers or reports errors if constraints are not met.
 * 4. Returns the appropriate execution state.
 *
 * Exception handling:
 * Includes a mechanism to handle exceptions at a granular argument level or globally using exception
 * handler methods. Proper fallback logic ensures errors are captured and processed effectively.
 *
 * Tab completion:
 * Dynamically generates suggestions for commands based on the current input and supports player name
 * retrieval or other customization points for argument completions.
 *
 * The class also features annotations to facilitate metadata about the command, such as its name,
 * description, usage, and required permission.
 */
abstract class VitalCommand<P, CS : Any> protected constructor(
    val plugin: P,
    val commandSenderClass: Class<CS>,
) {
    val name: String
    val permission: String
    val playerOnly: Boolean
    val args: Map<Pattern, Arg>
    val argHandlers: Map<Arg, ArgHandlerContext>
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
     *
     * @param commandSender The command sender to check.
     * @return True if the command sender is identified as a player, false otherwise.
     */
    abstract fun isPlayer(commandSender: CS): Boolean

    /**
     * Checks whether the specified command sender has the required permission.
     *
     * @param commandSender The command sender whose permissions are being checked.
     * @param permission The permission string to check against.
     * @return True if the command sender has the specified permission, false otherwise.
     */
    abstract fun hasPermission(
        commandSender: CS,
        permission: String,
    ): Boolean

    /**
     * Retrieves a list of all player names.
     *
     * @return A list of strings representing the names of all players.
     */
    abstract fun getAllPlayerNames(): List<String>

    /**
     * Retrieves a command argument value based on the executed argument string.
     *
     * The method filters through the entries in `args` and identifies an entry
     * where the key's matcher matches the provided executed argument string.
     * It then maps the matching entry's value, returning the first matching value found.
     *
     * @param executedArg The argument string that was executed. Used to search for a matching entry in the `args` map.
     * @return The first matching argument value or null if no match is found.
     */
    internal fun getArg(executedArg: String) =
        args.entries
            .filter { it.key.matcher(executedArg).matches() }
            .map { it.value }
            .firstOrNull()

    /**
     * Executes the global exception handler method for handling exceptions thrown during command execution.
     *
     * This method attempts to retrieve a global exception handler for the provided exception type.
     * If a relevant handler is found, it invokes the handler method with the appropriate parameters
     * derived from the context.
     *
     * If no handler is found, it delegates the error handling to the `onCommandError` method.
     *
     * If an exception occurs while invoking the handler method, a `VitalCommandException.ExecuteGlobalExceptionHandlerMethod`
     * is thrown.
     *
     * @param sender The command sender who initiated the command execution.
     * @param executedArg The argument string associated with the command execution.
     * @param commandArg The argument metadata associated with the command, or null if not applicable.
     * @param exception The exception that was thrown during command execution.
     */
    private fun executeGlobalExceptionHandlerMethod(
        sender: CS,
        executedArg: String,
        commandArg: Arg?,
        exception: Throwable,
    ) {
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
     *
     * This method attempts to find and invoke an exception handler tied to the particular argument's execution context.
     * If no specific handler is found, the global exception handler is used as a fallback.
     *
     * If the exception handler invocation fails or an error occurs during its execution, a `VitalCommandException.ExecuteArgExceptionHandlerMethod`
     * is thrown with details about the handler method and context.
     *
     * @param sender The command sender who attempted to execute the command.
     * @param originalException The exception that was thrown during command execution.
     * @param executedArg The string representation of the executed argument related to the exception.
     * @param commandArg The metadata of the command argument associated with the exception.
     */
    private fun executeArgExceptionHandlerMethod(
        sender: CS,
        originalException: Throwable,
        executedArg: String,
        commandArg: Arg?,
    ) {
        // when passing an invocation target exception, we first have to extract the actual exception that occurred.
        var exception = originalException
        if (exception is InvocationTargetException) {
            var extractedException = originalException.targetException
            while (extractedException is InvocationTargetException) {
                extractedException = extractedException.targetException
            }

            exception = extractedException
        }

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
            throw VitalCommandException.ExecuteArgExceptionHandlerMethod(context.handlerMethod, context, e)
        }
    }

    /**
     * Executes the handler method associated with a specific command argument.
     *
     * This method retrieves the handler for the provided command argument and invokes it with the necessary parameters.
     * If no handler is mapped for the argument, an exception is thrown.
     *
     * @param sender The command sender who invoked the command.
     * @param executedArg The executed argument string associated with the command.
     * @param commandArg The metadata of the command argument being processed.
     * @param values Additional argument values passed during the command execution.
     * @return The result of the handler method execution as a [ReturnState].
     * @throws VitalCommandException.UnmappedArgHandler if no handler is mapped for the provided argument.
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
     *
     * @param sender The command sender requesting tab completion. This could be a player or console sender.
     * @param args The arguments that have been entered so far by the sender. Used to determine matching commands or options.
     * @return A list of suggested completions based on the current input context.
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
     * Executes a command with the given sender and arguments, handling permissions, player-specific checks,
     * and individual argument execution handlers. Supports handling base commands as well as sub-commands.
     *
     * @param sender The command sender instance (either a player or console executor).
     * @param args An array of arguments passed to the command.
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
                ReturnState.SUCCESS -> run {}
                ReturnState.INVALID_ARGS -> onCommandInvalidArgs(sender, joinedPlayerArgs)
                ReturnState.NO_PERMISSION -> onCommandRequiresPermission(sender, joinedPlayerArgs, matchedArg)
                ReturnState.ONLY_PLAYER -> onCommandRequiresPlayer(sender, joinedPlayerArgs, matchedArg)
            }
        } catch (e: Exception) {
            executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, matchedArg)
        }
    }

    /**
     * Handles an error that occurs during the execution of a command.
     *
     * This method is invoked when an exception is thrown while a command is being executed.
     * It can be overridden to provide custom error handling logic.
     *
     * @param sender The command sender who initiated the command.
     * @param commandArg The argument associated with the command that caused the error, or null if not applicable.
     * @param e The exception thrown during command execution.
     * @return Unit. This method does not return any specific value.
     */
    protected open fun onCommandError(
        sender: CS,
        commandArg: Arg?,
        e: Throwable,
    ): Unit = throw e

    /**
     * Handles the tab-completion logic for a command.
     *
     * This method is typically invoked when a user attempts to tab-complete a command.
     * It can be overridden to provide custom tab-completion behavior based on the sender
     * and the current argument string.
     *
     * @param sender The command sender who initiated the tab-complete request.
     * @param args The current argument string for which tab-completion is being requested.
     * @return A list of possible tab-completion suggestions for the given input.
     */
    protected open fun onCommandTabComplete(
        sender: CS,
        args: String,
    ) = listOf<String>()

    /**
     * Handles the scenario where a command is executed with invalid arguments.
     *
     * This method is invoked when the provided arguments for a command are considered invalid
     * or do not match the expected format. It can be overridden to customize the behavior
     * when such a case occurs.
     *
     * @param sender The command sender who issued the command.
     * @param args The arguments that were deemed invalid.
     */
    protected open fun onCommandInvalidArgs(
        sender: CS,
        args: String,
    ) {
    }

    /**
     * Handles the scenario where a command requires a specific permission to be executed.
     *
     * This method is invoked when a command sender attempts to execute a command
     * or subcommand and lacks the necessary permission to do so. It can be overridden
     * to define custom behavior for handling cases where permission is insufficient.
     *
     * @param sender The command sender who attempted to execute the command.
     * @param args The arguments provided with the command execution.
     * @param commandArg The associated command argument metadata, or null if not applicable.
     */
    protected open fun onCommandRequiresPermission(
        sender: CS,
        args: String,
        commandArg: Arg?,
    ) {
    }

    /**
     * Handles the scenario where a command requires the sender to be a player.
     *
     * This method is invoked when a command or subcommand has a player-only restriction,
     * and the sender is not identified as a player. Override this method to provide
     * custom behavior for handling such cases.
     *
     * @param sender The command sender who attempted to execute the command.
     * @param args The arguments provided with the command execution.
     * @param commandArg The associated command argument metadata, or null if not applicable.
     */
    protected open fun onCommandRequiresPlayer(
        sender: CS,
        args: String,
        commandArg: Arg?,
    ) {
    }

    /**
     * Represents the possible states returned by the execution of a command.
     *
     * These states indicate the result or outcome of the command execution process,
     * providing context for handling different scenarios during command execution.
     */
    enum class ReturnState {
        /**
         * Indicates that a command execution failed due to invalid arguments being provided.
         *
         * This state is returned when the arguments supplied for a command do not match the expected
         * format, type, or constraints required for successful execution. It is commonly used
         * to signal argument-related errors during command processing.
         */
        INVALID_ARGS,

        /**
         * Indicates that the execution of the command was successful.
         *
         * Represents a state where the command completed its operation as expected
         * without any errors or exceptional scenarios.
         */
        SUCCESS,

        /**
         * Indicates that the command execution failed because the executor does not have
         * the required permission to perform the action.
         *
         * This state is generally returned when the command sender lacks the necessary
         * authorization or privileges to execute the command successfully.
         */
        NO_PERMISSION,

        /**
         * Represents a return state indicating that the executed command can only be used by a player.
         *
         * This state is used to enforce that specific commands are restricted to in-game players
         * and cannot be executed by non-player entities, such as the server console or command blocks.
         */
        ONLY_PLAYER,
    }

    /**
     * Annotation used to provide metadata for command classes.
     *
     * This annotation is typically applied to classes that define commands
     * in the application, allowing additional descriptive and configuration
     * details to be specified for the command.
     *
     * @property name The primary name of the command. This is the unique identifier used to execute the command.
     * @property description A brief description of the command. Defaults to "A Vital Command".
     * @property aliases An array of alternative names or shortcuts for the command. Defaults to an empty array.
     * @property usage Instructions or examples of how to use the command. Defaults to an empty string.
     * @property permission The required permission to execute the command. Defaults to an empty string, meaning no specific permission is required.
     * @property playerOnly Indicates whether the command can only be executed by players. Defaults to true, meaning non-player entities cannot execute the command.
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
     * Annotation for defining metadata about command arguments.
     *
     * This annotation is applied to argument handlers for commands and provides
     * configuration options such as name, required permission, and whether the command is restricted
     * to player senders only.
     *
     * If passing an empty string, the command sender must execute the command without an argument
     * for the annotated member to be invoked.
     *
     * @property name The name of the argument, empty for no argument.
     * @property permission The required permission to use this argument. Defaults to an empty string,
     * meaning no specific permission is required.
     * @property playerOnly Specifies whether the command argument is restricted to player senders only.
     * Defaults to `false`.
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
     * Annotation for marking a function as an argument handler in a command system.
     *
     * This annotation is used to associate a command argument, defined by [Arg], with
     * a handling function. The annotated function will handle the processing logic when
     * the specified argument is executed within a command.
     *
     * @property arg The [Arg] associated with this handler. This defines the metadata for
     * the command argument that this function is designed to handle.
     */
    @Repeatable
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.FUNCTION)
    annotation class ArgHandler(
        val arg: Arg,
    )

    /**
     * Marks a method as an exception handler specifically for command arguments in the `VitalCommand` class.
     *
     * This annotation is used to associate a specific method with handling exceptions to a certain type
     * that occur for a specified command argument during command execution.
     *
     * @property arg The name of the command argument for which this handler should be invoked.
     * @property type The class type of the exception this handler is designed to handle.
     */
    @Repeatable
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class ArgExceptionHandler(
        val arg: String,
        val type: KClass<out Throwable>,
    )

    /**
     * Annotation used to define an advice class for handling global exception handlers across commands.
     *
     * Serves a similar purpose to the `@RestControllerAdvice` annotation in Spring, providing a way
     * to centralize error handling logic for command execution within the framework.
     *
     * @property commandSenderClass Specifies the class type for the command sender associated with the advice.
     */
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Advice(
        val commandSenderClass: KClass<out Any>,
    )

    /**
     * Annotation that marks a method as a handler for specific exceptions occurring during command execution.
     *
     * This annotation can be applied to methods to designate them as global exception handlers for a specific type of
     * [Throwable]. The annotated method will be triggered when an exception to the specified type is encountered, allowing
     * for centralized error handling logic in the context of command execution.
     *
     * @property type The class of the exception type that this handler will manage.
     */
    @Repeatable
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class GlobalExceptionHandler(
        val type: KClass<out Throwable>,
    )

    /**
     * Context for handling global exceptions in the execution of commands.
     *
     * This data class provides detailed information required to invoke a global
     * exception handler method. It encapsulates references to the handler instance,
     * method, and the specific indexes of related parameters for flexible processing
     * during exception handling workflows.
     *
     * @property adviceInstance The instance containing the handler method to be invoked.
     * @property handlerMethod The method responsible for handling exceptions globally.
     * @property commandSenderIndex The index of the command sender parameter in the handler method, or null if not applicable.
     * @property executedArgIndex The index of the executed argument in the handler method, or null if not applicable.
     * @property commandArgIndex The index of the command argument parameter in the handler method, or null if not applicable.
     * @property valuesIndex The index of the values parameter in the handler method, or null if not applicable.
     * @property exceptionIndex The index of the exception parameter in the handler method, or null if not applicable.
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
     * Represents the context for tab-completion during command execution.
     *
     * This class is used to provide information about possible tab-completions for a command,
     * including the list of completion suggestions and available player names.
     *
     * @property completions A mutable list of suggestions for tab-completion,
     * which can be dynamically modified to include or exclude specific options.
     * @property playerNames A list of player names available for tab-completion, typically
     * used for commands where player-specific input is required.
     */
    data class TabCompletionContext(
        val completions: MutableList<String>,
        val playerNames: List<String>,
    )

    /**
     * A data class that encapsulates the contextual information required to handle
     * the execution of a command argument in a command-handling system.
     *
     * @property handlerMethod Represents the method responsible for handling the command argument.
     * @property commandSenderIndex Index of the command sender parameter in the method arguments,
     * or null if not applicable.
     * @property executedArgIndex Index of the executed argument parameter in the method arguments,
     * or null if not applicable.
     * @property commandArgIndex Index of the command argument metadata parameter in the method arguments,
     * or null if not applicable.
     * @property valuesIndex Index of the values parameter in the method arguments, or null if not applicable.
     */
    data class ArgHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val valuesIndex: Int?,
    )

    /**
     * Represents the context for handling exceptions that occur while processing a specific argument
     * in a command within the command framework.
     *
     * This data class is used to store metadata about the exception handler method and its relevant indices
     * for resolving arguments and exceptions when a command argument handler fails.
     *
     * @property handlerMethod The method to be invoked for handling the exception.
     * @property commandSenderIndex The index of the command sender parameter in the handler method, or null if not applicable.
     * @property executedArgIndex The index of the executed argument in the handler method, or null if not applicable.
     * @property commandArgIndex The index of the command argument metadata in the handler method, or null if not applicable.
     * @property exceptionIndex The index of the exception parameter in the handler method, or null if not applicable.
     */
    data class ArgExceptionHandlerContext(
        val handlerMethod: Method,
        val commandSenderIndex: Int?,
        val executedArgIndex: Int?,
        val commandArgIndex: Int?,
        val exceptionIndex: Int?,
    )

    /**
     * Abstract implementation of a Spigot command, designed to integrate with Bukkit/Spigot's command system.
     *
     * This class provides utilities for handling commands, their execution, and tab completion,
     * abstracting away repetitive boilerplate while allowing extension for custom behavior.
     *
     * @constructor Instantiated with a reference to a Spigot plugin.
     * @param plugin The plugin instance that the command is part of.
     */
    abstract class Spigot(
        plugin: SpigotPlugin,
    ) : VitalCommand<SpigotPlugin, SpigotCommandSender>(plugin, SpigotCommandSender::class.java),
        VitalPluginCommand.Spigot {
        @PostConstruct
        fun init() = plugin.getCommand(name)!!.setExecutor(this)

        override fun onCommand(
            sender: SpigotCommandSender,
            command: Command,
            label: String,
            args: Array<String>,
        ) = true.also { execute(sender, args) }

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

    /**
     * Represents an abstract base class for defining BungeeCord commands with extended functionality.
     *
     * This class integrates with the Vital command system and provides additional handling
     * for BungeeCord-specific command operations, such as registering commands to the proxy
     * and facilitating tab completion. It serves as an entry point for implementing custom
     * commands within a BungeeCord plugin.
     *
     * @constructor Initializes the command with the specified BungeePlugin instance.
     * @param plugin The BungeePlugin instance associated with this command.
     */
    abstract class Bungee(
        plugin: BungeePlugin,
    ) : VitalCommand<BungeePlugin, BungeeCommandSender>(plugin, BungeeCommandSender::class.java) {
        private lateinit var command: VitalPluginCommand.Bungee

        @PostConstruct
        fun init() {
            setupCommand()
            plugin.proxy.pluginManager.registerCommand(plugin, command)
        }

        private fun setupCommand() {
            this.command =
                object : VitalPluginCommand.Bungee(name) {
                    override fun execute(
                        sender: BungeeCommandSender,
                        args: Array<String>,
                    ) = this@Bungee.execute(sender, args)

                    override fun onTabComplete(
                        sender: BungeeCommandSender,
                        args: Array<String>,
                    ) = this@Bungee.tabComplete(sender, args)
                }
        }

        override fun isPlayer(commandSender: BungeeCommandSender) = commandSender is BungeePlayer

        override fun hasPermission(
            commandSender: BungeeCommandSender,
            permission: String,
        ) = commandSender.hasPermission(permission)

        override fun getAllPlayerNames() = ProxyServer.getInstance().players.map { it.name }
    }

    companion object {
        /**
         * A constant representing the default space string used as a delimiter in textual operations.
         *
         * This regular expression can be used for splitting, matching, or validating strings
         * where spaces are used as a separating character.
         */
        const val SPACE_REGEX = " "

        /**
         * A regular expression pattern used for matching placeholder variables
         * with an asterisk (*) modifier. This pattern can be useful for identifying
         * variable arguments in command processing or template substitution scenarios.
         *
         * The pattern matches strings that start and end with `%`,
         * and contain no whitespace characters in between, followed by `*`.
         *
         * Example match: `%example%*`
         */
        const val VARARG_REGEX = "(%\\S*%\\*|<\\S*>\\*)"

        /**
         * A regular expression pattern used for parsing arguments in the format `%<value>%`
         * while excluding potential conflicts with escaped patterns such as `%%`.
         *
         * The pattern matches:
         * - A `%` character followed by any non-whitespace sequence.
         * - Ending with another `%` character, ensuring it is not immediately followed by `*`.
         */
        const val ARG_REGEX = "(%\\S*%(?!\\*)|<\\S*>(?!\\*))"

        /**
         * Represents the string used to replace or represent spaces in certain contexts.
         *
         * This constant is used to ensure consistent handling of spaces when
         * interacting with commands, processing arguments, or performing string manipulations.
         */
        const val SPACE_REPLACEMENT = " "

        /**
         * A constant string value used as a replacement for variable arguments (`...`) in command handling.
         *
         * This value is typically used as a placeholder or pattern to match multiple arguments in commands
         * that support or require variable-length input. It may be used for parsing, validation, or other
         * operational tasks associated with processing command arguments.
         */
        const val VARARG_REPLACEMENT = ".+"

        /**
         * The regular expression pattern used to identify and validate arguments within commands.
         *
         * This constant defines the regex "\\S+", which matches one or more non-whitespace characters.
         * It is primarily used to parse and handle arguments passed to commands in the containing class.
         *
         * Example use cases include:
         * - Identifying valid arguments in a command input.
         * - Replacing or matching arguments based on specific criteria.
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
        fun VitalCommand<*, *>.getMappedArgs() =
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
        fun VitalCommand<*, *>.getMappedArgHandlers() =
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
        fun VitalCommand<*, *>.getMappedArgExceptionHandlers(): MutableMap<
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
        fun Class<out VitalCommand<*, *>>.getInfo(): Info = getRequiredAnnotation<Info>()

        /**
         * Retrieves the VitalCommand.Info annotation associated with this class.
         *
         * @receiver the class of the VitalCommand instance for which the annotation is to be retrieved.
         * @return the VitalCommand.Info annotation of this class.
         */
        @JvmStatic
        fun KClass<out VitalCommand<*, *>>.getInfo(): Info = java.getInfo()

        /**
         * Retrieves the VitalCommand.Info annotation associated with this instance.
         *
         * @receiver the VitalCommand instance for which the annotation is to be retrieved.
         * @return the VitalCommand.Info annotation of this instance.
         */
        @JvmStatic
        fun VitalCommand<*, *>.getInfo(): Info = javaClass.getInfo()

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

        /**
         * Retrieves the VitalCommand.Advice annotation associated with this instance.
         *
         * @receiver the instance for which the annotation is to be retrieved.
         * @return the VitalCommand.Advice annotation of this instance.
         */
        @JvmStatic
        fun Any.getVitalCommandAdvice(): Advice = javaClass.getVitalCommandAdvice()
    }
}
