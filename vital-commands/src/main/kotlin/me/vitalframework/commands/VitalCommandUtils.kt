package me.vitalframework.commands

import me.vitalframework.commands.VitalCommand.Arg
import java.lang.reflect.Parameter

object VitalCommandUtils {
    fun getArgHandlerContext(vararg parameters: Parameter) {

    }

    fun getArgExceptionHandlerContext(vararg parameters: Parameter) {

    }

    fun getExceptionHandlerContext(commandSenderClass: Class<Any>, vararg parameters: Parameter) {
        var commandSenderIndex: Int? = null
        var executedArgIndex: Int? = null
        var commandArgIndex: Int? = null
        var valuesIndex: Int? = null
        var exceptionIndex: Int? = null

        for (parameter in parameters) {
            when {
                commandSenderClass.isAssignableFrom(parameter.type) -> commandSenderIndex =
                    parameters.indexOf(parameter)

                String::class.java.isAssignableFrom(parameter.type) -> executedArgIndex =
                    method.parameters.indexOf(parameter)

                Arg::class.java.isAssignableFrom(parameter.type) -> commandArgIndex =
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
    }
}