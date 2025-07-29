package me.vitalframework.commands

import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * Utility object for processing and handling commands and their arguments or exceptions defined
 * within the `VitalCommand` structure. This object provides methods to map command arguments,
 * argument handlers, and exception handlers, as well as extracting injectable parameters for
 * execution context.
 */
object VitalCommandUtils {
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
    fun VitalCommand<*, *>.getMappedArgs() = javaClass.methods
        .filter { it.getAnnotationsByType(VitalCommand.ArgHandler::class.java).size > 0 }
        .map { it.getAnnotationsByType(VitalCommand.ArgHandler::class.java).toList() }
        .flatten()
        .associate {
            Pattern.compile(
                it.arg.name
                    .replace(VitalCommand.SPACE_REGEX.toRegex(), VitalCommand.SPACE_REPLACEMENT)
                    .replace(VitalCommand.VARARG_REGEX.toRegex(), VitalCommand.VARARG_REPLACEMENT)
                    .replace(VitalCommand.ARG_REGEX.toRegex(), VitalCommand.ARG_REPLACEMENT)
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
    fun VitalCommand<*, *>.getMappedArgHandlers() = javaClass.methods
        .asSequence()
        .filter { it.getAnnotationsByType(VitalCommand.ArgHandler::class.java).size > 0 }
        .map { method -> method.getAnnotationsByType(VitalCommand.ArgHandler::class.java).map { method to it } }
        .flatten()
        .associate { (method, argHandler) ->
            // now we have a viable method ready for handling incoming arguments
            // we just need to filter out the injectable parameters for our method
            // since we only support a handful of injectable params for handler methods...
            argHandler.arg to getArgHandlerContext(commandSenderClass, method)
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
    fun getInjectableArgHandlerMethodParameters(
        context: VitalCommand.ArgHandlerContext,
        sender: Any,
        executedArg: String,
        commandArg: VitalCommand.Arg,
        values: Array<String>,
    ): Array<Any> {
        val injectableParameters = mutableMapOf<Int, Any>()

        context.commandSenderIndex?.let { injectableParameters[it] = sender }
        context.executedArgIndex?.let { injectableParameters[it] = executedArg }
        context.commandArgIndex?.let { injectableParameters[it] = commandArg }
        context.valuesIndex?.let { injectableParameters[it] = values }

        return injectableParameters.entries.sortedBy { it.key }.map { it.value }.toTypedArray()
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
    fun VitalCommand<*, *>.getMappedArgExceptionHandlers(): MutableMap<VitalCommand.Arg, MutableMap<Class<out Throwable>, VitalCommand.ArgExceptionHandlerContext>> {
        val mappedArgExceptionHandlers =
            mutableMapOf<VitalCommand.Arg, MutableMap<Class<out Throwable>, VitalCommand.ArgExceptionHandlerContext>>()

        javaClass.methods
            .filter { it.getAnnotationsByType(VitalCommand.ArgExceptionHandler::class.java).size > 0 }
            .map { method -> method.getAnnotationsByType(VitalCommand.ArgExceptionHandler::class.java).map { method to it } }
            .flatten()
            .forEach { (method, argExceptionHandler) ->
                val arg = getArg(argExceptionHandler.arg)
                    ?: throw VitalCommandException.UnmappedArgExceptionHandlerArg(method, argExceptionHandler.arg)
                val context = getArgExceptionHandlerContext(commandSenderClass, method)

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
    fun getInjectableArgExceptionHandlerMethodParameters(
        context: VitalCommand.ArgExceptionHandlerContext,
        sender: Any,
        executedArg: String,
        commandArg: VitalCommand.Arg,
        exception: Throwable,
    ): Array<Any> {
        val injectableParameters = mutableMapOf<Int, Any>()

        context.commandSenderIndex?.let { injectableParameters[it] = sender }
        context.executedArgIndex?.let { injectableParameters[it] = executedArg }
        context.commandArgIndex?.let { injectableParameters[it] = commandArg }
        context.exceptionIndex?.let { injectableParameters[it] = exception }

        return injectableParameters.entries.sortedBy { it.key }.map { it.value }.toTypedArray()
    }

    /**
     * Constructs an `ArgHandlerContext` for the given method of a command sender class.
     *
     * @param commandSenderClass The class type of the command sender.
     * @param method The method to analyze and build the `ArgHandlerContext`.
     * @return An `ArgHandlerContext` object that contains parsed parameter indexes for
     * handling command arguments.
     * @throws VitalCommandException.InvalidArgHandlerReturnSignature If the method does not return `VitalCommand.ReturnState`.
     * @throws VitalCommandException.InvalidArgHandlerParameterSignature If any method parameter has an invalid type.
     */
    fun getArgHandlerContext(commandSenderClass: Class<*>, method: Method): VitalCommand.ArgHandlerContext {
        if (method.returnType != VitalCommand.ReturnState::class.java) {
            throw VitalCommandException.InvalidArgHandlerReturnSignature(method, method.returnType)
        }
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var valuesIndex: Int? = null

        for (parameter in method.parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex = method.parameters.indexOf(parameter)
                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex = method.parameters.indexOf(parameter)
                VitalCommand.Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex = method.parameters.indexOf(parameter)
                Array<String>::class.java.isAssignableFrom(parameter.type) -> valuesIndex = method.parameters.indexOf(parameter)
                else -> throw VitalCommandException.InvalidArgHandlerParameterSignature(method, parameter)
            }
        }

        return VitalCommand.ArgHandlerContext(method, commandSenderIndex, executedArgIndex, commandArgIndex, valuesIndex)
    }

    /**
     * Constructs an `ArgExceptionHandlerContext` for a given method and command sender class. It identifies the indices
     * of necessary parameters such as the command sender, executed argument, command argument, and exception within the
     * provided method's parameter list.
     *
     * @param commandSenderClass The class of the command sender used to identify the corresponding parameter.
     * @param method The method whose parameters are analyzed to retrieve the context.
     * @return An instance of `VitalCommand.ArgExceptionHandlerContext` containing the indices of relevant parameters
     *         within the method's parameters.
     * @throws VitalCommandException.InvalidArgExceptionHandlerMethodSignature If a parameter of an invalid type is
     *         encountered in the method.
     */
    fun getArgExceptionHandlerContext(commandSenderClass: Class<*>, method: Method): VitalCommand.ArgExceptionHandlerContext {
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var exceptionIndex: Int? = null

        for (parameter in method.parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex = method.parameters.indexOf(parameter)
                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex = method.parameters.indexOf(parameter)
                VitalCommand.Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex = method.parameters.indexOf(parameter)
                Exception::class.java.isAssignableFrom(parameter.type) -> exceptionIndex = method.parameters.indexOf(parameter)
                else -> throw VitalCommandException.InvalidArgExceptionHandlerMethodSignature(method, parameter)
            }
        }

        return VitalCommand.ArgExceptionHandlerContext(method, commandSenderIndex, executedArgIndex, commandArgIndex, exceptionIndex)
    }

    /**
     * Constructs and returns a `GlobalExceptionHandlerContext` instance for managing a global exception handler.
     *
     * @param adviceInstance An instance of the advice that contains the exception handler method.
     * @param commandSenderClass The class type representing the command sender in the context of the global exception handler.
     * @param method The method to be analyzed for parameter indices and validated for its eligibility as a global exception handler.
     * @return A `GlobalExceptionHandlerContext` containing metadata about the exception handler such as parameter indices.
     * @throws VitalCommandException.InvalidGlobalExceptionHandlerMethodSignature If the method signature has unsupported parameter types.
     */
    fun getGlobalExceptionHandlerContext(
        adviceInstance: Any,
        commandSenderClass: Class<*>,
        method: Method,
    ): VitalCommand.GlobalExceptionHandlerContext {
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var valuesIndex: Int? = null
        var exceptionIndex: Int? = null

        for (parameter in method.parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex = method.parameters.indexOf(parameter)
                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex = method.parameters.indexOf(parameter)
                VitalCommand.Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex = method.parameters.indexOf(parameter)
                Array<String>::class.java.isAssignableFrom(parameter.type) -> valuesIndex = method.parameters.indexOf(parameter)
                Throwable::class.java.isAssignableFrom(parameter.type) -> exceptionIndex = method.parameters.indexOf(parameter)
                else -> throw VitalCommandException.InvalidGlobalExceptionHandlerMethodSignature(method, parameter)
            }
        }

        return VitalCommand.GlobalExceptionHandlerContext(
            adviceInstance,
            method,
            commandSenderIndex,
            executedArgIndex,
            commandArgIndex,
            valuesIndex,
            exceptionIndex
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
    fun getInjectableGlobalExceptionHandlerMethodParameters(
        context: VitalCommand.GlobalExceptionHandlerContext,
        sender: Any,
        executedArg: String,
        commandArg: VitalCommand.Arg?,
        exception: Throwable,
    ): Array<Any?> {
        val injectableParameters = mutableMapOf<Int, Any?>()

        context.commandSenderIndex?.let { injectableParameters[it] = sender }
        context.executedArgIndex?.let { injectableParameters[it] = executedArg }
        context.commandArgIndex?.let { injectableParameters[it] = commandArg }
        context.exceptionIndex?.let { injectableParameters[it] = exception }

        return injectableParameters.entries.sortedBy { it.key }.map { it.value }.toTypedArray()
    }
}