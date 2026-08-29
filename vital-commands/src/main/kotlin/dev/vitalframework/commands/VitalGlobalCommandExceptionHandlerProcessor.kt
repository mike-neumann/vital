package dev.vitalframework.commands

import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import dev.vitalframework.commands.VitalCommand.Companion.getGlobalExceptionHandlerContext
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils

open class VitalGlobalCommandExceptionHandlerProcessor(
    applicationContext: ApplicationContext,
    val commands: List<VitalCommand<*>>,
) : InitializingBean {
    private val advices = applicationContext.getBeansWithAnnotation<VitalCommand.Advice>().values

    final override fun afterPropertiesSet() {
        for (command in commands) {
            val adviceInstances =
                advices
                    .filter {
                        val info = it.javaClass.getRequiredAnnotation<VitalCommand.Advice>()
                        command.commandSenderClass.isAssignableFrom(info.commandSenderClass.java)
                    }

            // get all advices for the command sender of the command.
            for (adviceInstance in adviceInstances) {
                val advice = AnnotationUtils.getAnnotation(adviceInstance.javaClass, VitalCommand.Advice::class.java)!!
                val methodsAndExceptionsHandlers =
                    adviceInstance::class.java.methods
                        .filter { it.getAnnotationsByType(VitalCommand.GlobalExceptionHandler::class.java).size > 0 }
                        .flatMap { method ->
                            method
                                .getAnnotationsByType(VitalCommand.GlobalExceptionHandler::class.java)
                                .map { method to it }
                        }

                for ((method, exceptionHandler) in methodsAndExceptionsHandlers) {
                    globalExceptionHandlers[exceptionHandler.type.java] =
                        method.getGlobalExceptionHandlerContext(adviceInstance, advice.commandSenderClass.java)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        private val globalExceptionHandlers =
            mutableMapOf<Class<out Throwable>, VitalCommand.GlobalExceptionHandlerContext>()

        @JvmStatic
        fun getGlobalExceptionHandler(type: Class<out Throwable>) =
            globalExceptionHandlers.entries
                .find {
                    it.key.isAssignableFrom(type)
                }?.value
    }
}
