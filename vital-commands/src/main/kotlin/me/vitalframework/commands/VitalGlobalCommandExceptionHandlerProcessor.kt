package me.vitalframework.commands

import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class VitalGlobalCommandExceptionHandlerProcessor(applicationContext: ApplicationContext) {
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
                        .filter { it.isAnnotationPresent(VitalCommand.GlobalExceptionHandler::class.java) }
                        .map { method -> method.getAnnotationsByType(VitalCommand.GlobalExceptionHandler::class.java).map { method to it } }
                        .flatten()
                        .forEach { (method, exceptionHandler) ->
                            GLOBAL_EXCEPTION_HANDLERS[exceptionHandler.type.java] =
                                VitalCommandUtils.getGlobalExceptionHandlerContext(adviceInstance, advice.commandSenderClass.java, method)
                        }
                }
        }
    }

    companion object {
        private val GLOBAL_EXCEPTION_HANDLERS = mutableMapOf<Class<out Throwable>, VitalCommand.GlobalExceptionHandlerContext>()

        fun getGlobalExceptionHandler(type: Class<out Throwable>) =
            GLOBAL_EXCEPTION_HANDLERS.entries.find { it.key.isAssignableFrom(type) }?.value
    }
}