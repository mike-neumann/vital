package me.vitalframework.commands

import java.lang.reflect.Method
import java.lang.reflect.Parameter

abstract class VitalCommandException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    class ExecuteArgExceptionHandlerMethod(
        method: Method,
        context: VitalCommand.ArgExceptionHandlerContext,
        cause: Throwable,
    ) : VitalCommandException(
        "Error while executing arg exception handler method '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })' using context '$context'",
        cause
    )

    class UnmappedArgHandler(arg: String) : VitalCommandException("No arg handler method exists for arg '$arg'")

    class InvalidArgHandlerMethodSignature(method: Method, parameter: Parameter) : VitalCommandException(
        "Invalid arg handler method signature '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })', failed at '${parameter.type.simpleName} ${parameter.name}'"
    )

    class UnmappedArgExceptionHandlerArg(method: Method, arg: String) : VitalCommandException(
        "Arg exception handler mapping for method '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })' failed, arg '$arg' does not exist"
    )

    class InvalidArgExceptionHandlerMethodSignature(method: Method, parameter: Parameter) : VitalCommandException(
        "Invalid arg exception handler method signature '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })', failed at '${parameter.type.simpleName} ${parameter.name}'",
    )

    class InvalidExceptionHandlerMethodSignature(method: Method, parameter: Parameter) : VitalCommandException(
        "Invalid exception handler method signature '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })' failed at '${parameter.type.simpleName} ${parameter.name}'"
    )

    class ExecuteExceptionHandlerMethod(
        method: Method,
        context: VitalCommand.ExceptionHandlerContext,
        cause: Throwable
    ) : VitalCommandException(
        "Error while executing exception handler method '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })' using context '$context'",
        cause
    )
}