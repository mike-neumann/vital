package dev.vitalframework.tasks

import dev.vitalframework.BungeePlugin
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.VitalCoreModule.Companion.logger
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment
import java.lang.reflect.Method
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Internal class; used to define a custom schedular to schedule tasks on the server's schedular instead of the one from Spring.
 * This schedular can schedule functions annotated with [VitalScheduled].
 */
abstract class VitalScheduler(
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
                schedule { method.invoke(bean) }
            } catch (e: Exception) {
                logger.error("Error while processing Vital-scheduled task", e)
            }
        }, initialDelay, fixedDelay, timeUnit)
    }

    fun shutdown() {
        scheduler.shutdownNow()
    }

    abstract fun schedule(method: Runnable)

    class Spigot(
        environment: Environment,
        private val plugin: SpigotPlugin,
    ) : VitalScheduler(environment) {
        override fun schedule(method: Runnable) {
            Bukkit.getScheduler().runTaskLater(plugin, method, 0L)
        }
    }

    class Bungee(
        environment: Environment,
        private val plugin: BungeePlugin,
    ) : VitalScheduler(environment) {
        override fun schedule(method: Runnable) {
            ProxyServer.getInstance().scheduler.schedule(plugin, method, 0L, TimeUnit.NANOSECONDS)
        }
    }
}
