package me.myproject

import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule.Companion.getVitalInfo
import me.vitalframework.VitalCoreSubModule.Companion.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener

@Vital.Info(
    "{name}",
    "{description}",
    "{apiVersion}",
    "{version}",
    [{author}],
    {pluginEnvironment}
)
class MyKotlinPlugin {
    private val logger = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val info = MyKotlinPlugin::class.getVitalInfo()
        logger.info("Kotlin Vital plugin '${info.name}' version '${info.version}' successfully loaded!")
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosed() {
        val info = MyKotlinPlugin::class.getVitalInfo()
        logger.info("Kotlin Vital plugin '${info.name}' version '${info.version}' successfully unloaded!")
    }
}
