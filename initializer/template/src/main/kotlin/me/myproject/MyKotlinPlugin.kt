package me.myproject

import me.vitalframework.Vital
import me.vitalframework.VitalCoreModule.Companion.getVitalInfo
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
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
