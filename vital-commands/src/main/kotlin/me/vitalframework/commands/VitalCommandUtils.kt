package me.vitalframework.commands

import java.lang.reflect.Method
import java.util.regex.Pattern

object VitalCommandUtils {
    fun getMappedArgs(command: VitalCommand<*, *>) = command.javaClass.methods
        .filter { it.isAnnotationPresent(VitalCommand.ArgHandler::class.java) }
        .map { it.getAnnotation(VitalCommand.ArgHandler::class.java) }
        .associate {
            Pattern.compile(
                it.arg.value
                    .replace(" ".toRegex(), "[ ]")
                    .replace("%.+%[*]".toRegex(), "(.+)")
                    .replace("%.+%".toRegex(), "(\\\\S+)")
            ) to it.arg
        }

    fun getMappedArgHandlers(command: VitalCommand<*, *>) = command.javaClass.methods
        .filter { VitalCommand.ReturnState::class.java.isAssignableFrom(it.returnType) }
        .filter { it.isAnnotationPresent(VitalCommand.ArgHandler::class.java) }
        .associate { method ->
            // now we have a viable method ready for handling incoming arguments
            // we just need to filter out the injectable parameters for our method
            // since we only support a handful of injectable params for handler methods...
            val vitalCommandArg = method.getAnnotation(VitalCommand.ArgHandler::class.java).arg

            vitalCommandArg to getArgHandlerContext(command.commandSenderClass, method)
        }

    fun getInjectableArgHandlerMethodParameters(
        context: VitalCommand.ArgHandlerContext,
        sender: Any,
        executedArg: String,
        commandArg: VitalCommand.Arg,
        values: Array<String>,
    ) = let {
        val injectableParameters = mutableMapOf<Int, Any>()

        context.commandSenderIndex?.let { injectableParameters[it] = sender }
        context.executedArgIndex?.let { injectableParameters[it] = executedArg }
        context.commandArgIndex?.let { injectableParameters[it] = commandArg }
        context.valuesIndex?.let { injectableParameters[it] = values }

        injectableParameters.entries
            .sortedBy { it.key }
            .map { it.value }
            .toTypedArray()
    }

    fun getMappedArgExceptionHandlers(command: VitalCommand<*, *>) = let {
        val mappedArgExceptionHandlers =
            mutableMapOf<VitalCommand.Arg, MutableMap<Class<out Throwable>, VitalCommand.ArgExceptionHandlerContext>>()

        command.javaClass.methods
            .filter { it.isAnnotationPresent(VitalCommand.ArgExceptionHandler::class.java) }
            .forEach { method ->
                val argExceptionHandler = method.getAnnotation(VitalCommand.ArgExceptionHandler::class.java)!!
                val arg = command.getArg(argExceptionHandler.arg)
                    ?: throw VitalCommandException.UnmappedArgExceptionHandlerArg(
                        method,
                        argExceptionHandler.arg
                    )
                val context = getArgExceptionHandlerContext(command.commandSenderClass, method)

                if (!mappedArgExceptionHandlers.containsKey(arg)) {
                    mappedArgExceptionHandlers[arg] =
                        mutableMapOf(argExceptionHandler.type.java to context)
                } else {
                    mappedArgExceptionHandlers[arg]!![argExceptionHandler.type.java] =
                        context
                }
            }

        mappedArgExceptionHandlers
            .map { it.key to it.value.toMap() }
            .toMap()
    }

    fun getInjectableArgExceptionHandlerMethodParameters(
        context: VitalCommand.ArgExceptionHandlerContext,
        sender: Any,
        executedArg: String,
        commandArg: VitalCommand.Arg,
        exception: Throwable,
    ) = let {
        val injectableParameters = mutableMapOf<Int, Any>()

        context.commandSenderIndex?.let { injectableParameters[it] = sender }
        context.executedArgIndex?.let { injectableParameters[it] = executedArg }
        context.argIndex?.let { injectableParameters[it] = commandArg }
        context.exceptionIndex?.let { injectableParameters[it] = exception }
        injectableParameters.entries
            .sortedBy { it.key }
            .map { it.value }
            .toTypedArray()
    }

    fun getArgHandlerContext(commandSenderClass: Class<*>, method: Method) = let {
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var valuesIndex: Int? = null

        for (parameter in method.parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex =
                    method.parameters.indexOf(parameter)

                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex =
                    method.parameters.indexOf(parameter)

                VitalCommand.Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex =
                    method.parameters.indexOf(parameter)

                Array<String>::class.java.isAssignableFrom(parameter.type) -> valuesIndex =
                    method.parameters.indexOf(parameter)

                else -> throw VitalCommandException.InvalidArgHandlerMethodSignature(method, parameter)
            }
        }

        VitalCommand.ArgHandlerContext(
            method,
            commandSenderIndex,
            executedArgIndex,
            commandArgIndex,
            valuesIndex
        )
    }

    fun getArgExceptionHandlerContext(commandSenderClass: Class<*>, method: Method) = let {
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var exceptionIndex: Int? = null

        for (parameter in method.parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex =
                    method.parameters.indexOf(parameter)

                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex =
                    method.parameters.indexOf(parameter)

                VitalCommand.Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex =
                    method.parameters.indexOf(parameter)

                Exception::class.java.isAssignableFrom(parameter.type) -> exceptionIndex =
                    method.parameters.indexOf(parameter)

                else -> throw VitalCommandException.InvalidArgExceptionHandlerMethodSignature(
                    method,
                    parameter
                )
            }
        }

        VitalCommand.ArgExceptionHandlerContext(
            method,
            commandSenderIndex,
            executedArgIndex,
            commandArgIndex,
            exceptionIndex
        )
    }

    fun getExceptionHandlerContext(commandSenderClass: Class<*>, method: Method) = let {
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var valuesIndex: Int? = null
        var exceptionIndex: Int? = null

        for (parameter in method.parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex =
                    method.parameters.indexOf(parameter)

                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex =
                    method.parameters.indexOf(parameter)

                VitalCommand.Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex =
                    method.parameters.indexOf(parameter)

                Array<String>::class.java.isAssignableFrom(parameter.type) -> valuesIndex =
                    method.parameters.indexOf(parameter)

                Throwable::class.java.isAssignableFrom(parameter.type) -> exceptionIndex =
                    method.parameters.indexOf(parameter)

                else -> throw VitalCommandException.InvalidExceptionHandlerMethodSignature(
                    method,
                    parameter
                )
            }
        }

        VitalCommand.ExceptionHandlerContext(
            method,
            commandSenderIndex,
            executedArgIndex,
            commandArgIndex,
            valuesIndex,
            exceptionIndex
        )
    }

    fun getInjectableExceptionHandlerMethodParameters(
        context: VitalCommand.ExceptionHandlerContext,
        sender: Any,
        executedArg: String,
        commandArg: VitalCommand.Arg,
        exception: Throwable,
    ) = let {
        val injectableParameters = mutableMapOf<Int, Any>()

        context.commandSenderIndex?.let { injectableParameters[it] = sender }
        context.executedArgIndex?.let { injectableParameters[it] = executedArg }
        context.commandArgIndex?.let { injectableParameters[it] = commandArg }
        context.exceptionIndex?.let { injectableParameters[it] = exception }
        injectableParameters.entries
            .sortedBy { it.key }
            .map { it.value }
            .toTypedArray()
    }
}