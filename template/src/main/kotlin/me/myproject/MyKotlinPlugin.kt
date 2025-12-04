package me.myproject

import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule.Companion.getVitalInfo
import me.vitalframework.VitalCoreSubModule.Companion.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener

// TODO: You can delete the whole "kotlin" directory, if you don't want to use Kotlin for your plugin.
@Vital.Info(
    "MyKotlinPlugin",
    "MyKotlinPluginDescription",
    "1.21",
    "1.0.0",
    ["Me"],
    // TODO: Change this to your actual plugin environment: SPIGOT, PAPER or BUNGEE
    Vital.Info.PluginEnvironment.SPIGOT
)
class MyKotlinPlugin {
    private val logger = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val info = MyKotlinPlugin::class.getVitalInfo()
        logger.info("Vital plugin '${info.name}' version '${info.version}' successfully loaded!")
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosed() {
        val info = MyKotlinPlugin::class.getVitalInfo()
        logger.info("Vital plugin '${info.name}' version '${info.version}' successfully unloaded!")
    }
}
