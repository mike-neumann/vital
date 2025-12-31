package me.myproject

import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent

@Vital.Info(
    name = "{name}",
    description = "{description}",
    apiVersion = "{apiVersion}",
    version = "{version}",
    author = [{author}],
    environment = {pluginEnvironment}
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
