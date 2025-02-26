package me.vitalframework.commands

import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class VitalCommandExceptionHandlerProcessor(applicationContext: ApplicationContext) {
    private val commands = applicationContext.getBeansOfType(VitalCommand::class.java).values
    private val advices = applicationContext.getBeansWithAnnotation(VitalCommand.Advice::class.java).values

    @PostConstruct
    fun init() {
        for (command in commands) {
            // get all advices for the command sender of the command.
            advices
                .filter {
                    command.commandSenderClass.isAssignableFrom(it::class.java.getAnnotation(VitalCommand.Advice::class.java).commandSenderClass.java)
                }
                .forEach { adviceInstance ->
                    val advice = adviceInstance.javaClass.getAnnotation(VitalCommand.Advice::class.java)!!

                    adviceInstance::class.java.methods
                        .filter { it.isAnnotationPresent(VitalCommand.ExceptionHandler::class.java) }
                        .forEach { method ->
                            val annotation = method.getAnnotation(VitalCommand.ExceptionHandler::class.java)!!

                            EXCEPTION_HANDLERS[annotation.type.java] =
                                VitalCommandUtils.getExceptionHandlerContext(
                                    adviceInstance,
                                    advice.commandSenderClass.java,
                                    method
                                )
                        }
                }
        }
    }

    companion object {
        private val EXCEPTION_HANDLERS =
            mutableMapOf<Class<out Throwable>, VitalCommand.ExceptionHandlerContext>()

        fun getExceptionHandler(type: Class<out Throwable>) =
            EXCEPTION_HANDLERS.entries.find { type.isAssignableFrom(it.key) }?.value
    }
}