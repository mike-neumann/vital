package me.vitalframework.commands

import java.lang.reflect.Method
import java.util.regex.Pattern

object VitalCommandUtils {
    fun VitalCommand<*, *>.getMappedArgs() = javaClass.methods
        .filter { it.isAnnotationPresent(VitalCommand.ArgHandler::class.java) }
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

    fun VitalCommand<*, *>.getMappedArgHandlers() = javaClass.methods
        .asSequence()
        .filter { it.isAnnotationPresent(VitalCommand.ArgHandler::class.java) }
        .map { method -> method.getAnnotationsByType(VitalCommand.ArgHandler::class.java).map { method to it } }
        .flatten()
        .associate { (method, argHandler) ->
            // now we have a viable method ready for handling incoming arguments
            // we just need to filter out the injectable parameters for our method
            // since we only support a handful of injectable params for handler methods...
            argHandler.arg to getArgHandlerContext(commandSenderClass, method)
        }

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

    fun VitalCommand<*, *>.getMappedArgExceptionHandlers(): MutableMap<VitalCommand.Arg, MutableMap<Class<out Throwable>, VitalCommand.ArgExceptionHandlerContext>> {
        val mappedArgExceptionHandlers =
            mutableMapOf<VitalCommand.Arg, MutableMap<Class<out Throwable>, VitalCommand.ArgExceptionHandlerContext>>()

        javaClass.methods
            .filter { it.isAnnotationPresent(VitalCommand.ArgExceptionHandler::class.java) }
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