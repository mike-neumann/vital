package me.myproject

import me.vitalframework.Vital
import me.vitalframework.VitalCoreSubModule
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener

@Vital.Info(
    name = "${name}",
    description = "${description}",
    apiVersion = "${apiVersion}",
    version = "${version}",
    author = [${authors?map(it -> "\"" + it +  "\"")?join(", ")}],
    environment = Vital.PluginEnvironment.${pluginEnvironment}
)
class MyGroovyPlugin {
    private final def logger = VitalCoreSubModule.logger(this)

    @EventListener(ApplicationReadyEvent)
    final def onApplicationReady() {
        final def info = VitalCoreSubModule.getVitalInfo(MyGroovyPlugin)
        logger.info("Groovy Vital plugin '${r"${info.name()}"}' version '${r"${info.version()}"}' successfully loaded!")
    }

    @EventListener(ContextClosedEvent)
    final def onContextClosed() {
        final def info = VitalCoreSubModule.getVitalInfo(MyGroovyPlugin)
        logger.info("Groovy Vital plugin '${r"${info.name()}"}' version '${r"${info.version()}"}' successfully unloaded!")
    }
}
