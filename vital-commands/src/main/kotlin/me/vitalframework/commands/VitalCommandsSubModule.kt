package me.vitalframework.commands

import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException

@Component("vital-commands")
class VitalCommandsSubModule : VitalSubModule() {
    companion object {
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
    }
}
