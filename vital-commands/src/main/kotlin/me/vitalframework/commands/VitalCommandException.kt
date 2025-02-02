package me.vitalframework.commands

import java.lang.reflect.Method
import java.lang.reflect.Parameter

abstract class VitalCommandException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    class ExecuteArgExceptionHandler(
        argExceptionHandlerContext: VitalCommand.ArgExceptionHandlerContext,
        cause: Throwable,
    ) : VitalCommandException(
        "Error while executing arg exception handler method using context '$argExceptionHandlerContext'",
        cause
    )

    class UnmappedArgHandler(arg: String) : VitalCommandException("No handler method exists for arg '$arg'")

    class InvalidArgHandlerSignature(method: Method, parameter: Parameter) : VitalCommandException(
        "Invalid arg handler signature '${method.name}(${
            method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
        })', failed at '${parameter.type.simpleName} ${parameter.name}'"
    )

    class UnmappedArgExceptionHandlerArg(arg: String) :
        VitalCommandException("Exception handler mapping failed, arg '$arg' does not exist")

    class InvalidArgExceptionHandlerSignature(method: Method, parameter: Parameter) :
        VitalCommandException(
            "Invalid arg exception handler signature '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })', failed at '${parameter.type.simpleName} ${parameter.name}'",
        )
}