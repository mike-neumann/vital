package me.vitalframework.commands

import java.lang.reflect.Method

abstract class VitalCommandException(message: String) : RuntimeException() {
    class InvalidArgHandlerSignature(method: Method, failedAt) : VitalCommandException(
        "Invalid arg handler signature '${method.name}(${
            method.parameters.map { "${it.type.simpleName} ${it.name}" }.joinToString(", ")
        })', failed at '${it.type.simpleName} ${it.name}'"
    )
    class InvalidArgExceptionHandlerSignature(method: Method) : VitalCommandException()
}