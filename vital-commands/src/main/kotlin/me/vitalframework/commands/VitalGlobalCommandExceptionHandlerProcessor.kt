package me.vitalframework.commands

import me.vitalframework.commands.VitalCommand.Companion.getGlobalExceptionHandlerContext
import me.vitalframework.commands.VitalCommand.Companion.getVitalCommandAdvice
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class VitalGlobalCommandExceptionHandlerProcessor(
    applicationContext: ApplicationContext,
    val commands: List<VitalCommand<*>>,
) : InitializingBean {
    private val advices = applicationContext.getBeansWithAnnotation<VitalCommand.Advice>().values

    final override fun afterPropertiesSet() {
        for (command in commands) {
            // get all advices for the command sender of the command.
            advices
                .filter {
                    command.commandSenderClass.isAssignableFrom(
                        it.javaClass
                            .getVitalCommandAdvice()
                            .commandSenderClass.java,
                    )
                }.forEach { adviceInstance ->
                    val advice = adviceInstance.javaClass.getAnnotation(VitalCommand.Advice::class.java)!!
                    adviceInstance::class.java.methods
                        .filter { it.getAnnotationsByType(VitalCommand.GlobalExceptionHandler::class.java).size > 0 }
                        .map { method ->
                            method
                                .getAnnotationsByType(VitalCommand.GlobalExceptionHandler::class.java)
                                .map { method to it }
                        }.flatten()
                        .forEach { (method, exceptionHandler) ->
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
