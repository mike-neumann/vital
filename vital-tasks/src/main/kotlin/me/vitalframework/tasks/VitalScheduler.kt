package me.vitalframework.tasks

import me.vitalframework.VitalCoreSubModule.Companion.logger
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment
import java.lang.reflect.Method
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class VitalScheduler(
    val environment: Environment,
) : BeanPostProcessor {
    private val logger = logger()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String,
    ): Any? {
        for (method in bean.javaClass.methods) {
            val annotation = AnnotationUtils.getAnnotation(method, VitalScheduled::class.java)
            if (annotation != null) {
                schedule(bean, method, annotation)
            }
        }

        return bean
    }

    private fun schedule(
        bean: Any,
        method: Method,
        annotation: VitalScheduled,
    ) {
        val initialDelay =
            if (annotation.initialDelayString.isEmpty()) {
                annotation.initialDelay
            } else {
                environment
                    .resolveRequiredPlaceholders(
                        annotation.initialDelayString,
                    ).toLong()
            }
        val fixedDelay =
            if (annotation.fixedDelayString.isEmpty()) {
                annotation.fixedDelay
            } else {
                environment
                    .resolveRequiredPlaceholders(
                        annotation.fixedDelayString,
                    ).toLong()
            }
        val timeUnit =
            if (annotation.timeUnitString.isEmpty()) {
                annotation.timeUnit
            } else {
                TimeUnit.valueOf(
                    environment.resolveRequiredPlaceholders(annotation.timeUnitString),
                )
            }

        scheduler.scheduleWithFixedDelay({
            try {
                method.invoke(bean)
            } catch (e: Exception) {
                logger.error("Error while processing Vital-scheduled task", e)
            }
        }, initialDelay, fixedDelay, timeUnit)
    }
}
