package dev.vitalframework.commands

import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 * Internal exception; thrown during [VitalCommand] lifecycles.
 */
abstract class VitalCommandException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown when an exception occurs while a [VitalCommand] tries to execute an arg exception handler function.
     */
    class ExecuteArgExceptionHandlerMethod(
        method: Method,
        context: VitalCommand.ArgExceptionHandlerContext,
        cause: Throwable,
    ) : VitalCommandException(
            "Error while executing arg exception handler method '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })' using context '$context'",
            cause,
        )

    /**
     * Internal exception; thrown when no arg handler was found during [VitalCommand] execution.
     */
    class UnmappedArgHandler(
        arg: String,
    ) : VitalCommandException("No arg handler method exists for arg '$arg'")

    /**
     * Internal exception; thrown when an arg handler function was defined with an invalid or not-supported return type signature.
     * This exception is thrown directly when Vital starts up to reduce more runtime errors.
     */
    class InvalidArgHandlerReturnSignature(
        method: Method,
        returnType: Class<*>,
    ) : VitalCommandException(
            "Invalid arg handler return signature '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })', found '${returnType.name}', must be '${VitalCommand.ReturnState::class.java.name}'",
        )

    /**
     * Internal exception; thrown when an arg handler function was defined with an invalid or not-supported parameter signature.
     * This exception is thrown directly when Vital starts up to reduce more runtime errors.
     */
    class InvalidArgHandlerParameterSignature(
        method: Method,
        parameter: Parameter,
    ) : VitalCommandException(
            "Invalid arg handler parameter signature '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })', failed at '${parameter.type.simpleName} ${parameter.name}'",
        )

    /**
     * Internal exception; thrown when an arg exception handler was mapped to a non-existing arg / arg handler name.
     * This exception is thrown directly when Vital starts up to reduce more runtime errors.
     */
    class UnmappedArgExceptionHandlerArg(
        method: Method,
        arg: String,
    ) : VitalCommandException(
            "Arg exception handler mapping for method '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })' failed, arg '$arg' does not exist",
        )

    /**
     * Internal exception; thrown when an arg exception handler function was defined with an invalid or not-supported signature.
     * This exception is thrown directly when Vital starts up to reduce more runtime errors.
     */
    class InvalidArgExceptionHandlerMethodSignature(
        method: Method,
        parameter: Parameter,
    ) : VitalCommandException(
            "Invalid arg exception handler method signature '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })', failed at '${parameter.type.simpleName} ${parameter.name}'",
        )

    /**
     * Internal exception; thrown when a global exception handler function was defined with an invalid or not-supported signature.
     * This exception is thrown directly when Vital starts up to reduce more runtime errors.
     */
    class InvalidGlobalExceptionHandlerMethodSignature(
        method: Method,
        parameter: Parameter,
    ) : VitalCommandException(
            "Invalid global exception handler method signature '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })' failed at '${parameter.type.simpleName} ${parameter.name}'",
        )

    /**
     * Internal exception; thrown when an exception occurs while a [VitalCommand] tries to execute a global exception handler function.
     */
    class ExecuteGlobalExceptionHandlerMethod(
        method: Method,
        context: VitalCommand.GlobalExceptionHandlerContext,
        cause: Throwable,
    ) : VitalCommandException(
            "Error while executing global exception handler method '${method.name}(${
                method.parameters.joinToString(", ") { "${it.type.simpleName} ${it.name}" }
            })' using context '$context'",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs while registering a command.
     */
    class Register(
        commandClass: Class<out VitalCommand<*>>,
        cause: Throwable,
    ) : VitalCommandException("Error while registering command '${commandClass.simpleName}'.", cause)
}
