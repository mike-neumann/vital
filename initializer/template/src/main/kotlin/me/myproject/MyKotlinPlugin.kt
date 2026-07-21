package me.myproject

import dev.vitalframework.Vital
import dev.vitalframework.VitalCoreModule.Companion.getVitalInfo
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener

@Vital.Info(
    "${name}",
    "${description}",
    "${apiVersion}",
    "${version}",
    [${authors?map(it -> "\"" + it +  "\"")?join(", ")}],
    Vital.PluginEnvironment.${pluginEnvironment}
)
class MyKotlinPlugin {
    private val logger = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val info = MyKotlinPlugin::class.getRequiredAnnotation<Vital.Info>()
        logger.info("Kotlin Vital plugin '${r"${info.name}"}' version '${r"${info.version}"}' successfully loaded!")
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosed() {
        val info = MyKotlinPlugin::class.getRequiredAnnotation<Vital.Info>()
        logger.info("Kotlin Vital plugin '${r"${info.name}"}' version '${r"${info.version}"}' successfully unloaded!")
    }
}
