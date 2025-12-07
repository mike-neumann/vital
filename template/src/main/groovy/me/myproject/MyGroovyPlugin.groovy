package me.myproject

import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener

// TODO: You can delete the whole "groovy" directory, if you don't want to use Groovy for your plugin.
@Vital.Info(
    name = "MyGroovyPlugin",
    description = "MyGroovyPluginDescription",
    apiVersion = "1.21",
    version = "1.0.0",
    author = ["Me"],
    // TODO: Change this to your actual plugin environment: SPIGOT, PAPER or BUNGEE
    environment = Vital.Info.PluginEnvironment.PAPER
)
class MyGroovyPlugin {
    private final def logger = VitalCoreSubModule.logger(this)

    @EventListener(ApplicationReadyEvent)
    final def onApplicationReady() {
        final def info = VitalCoreSubModule.getVitalInfo(MyGroovyPlugin)
        logger.info("Groovy Vital plugin '${info.name()}' version '${info.version()}' successfully loaded!")
    }

    @EventListener(ContextClosedEvent)
    final def onContextClosed() {
        final def info = VitalCoreSubModule.getVitalInfo(MyGroovyPlugin)
        logger.info("Groovy Vital plugin '${info.name()}' version '${info.version()}' successfully unloaded!")
    }
}
