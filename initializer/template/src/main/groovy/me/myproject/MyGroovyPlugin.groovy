package me.myproject

import dev.vitalframework.Vital
import dev.vitalframework.VitalCoreModule
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
    private final def logger = VitalCoreModule.logger(this)

    @EventListener(ApplicationReadyEvent)
    final def onApplicationReady() {
        final def info = VitalCoreModule.getRequiredAnnotation(MyGroovyPlugin, Vital.Info)
        logger.info("Groovy Vital plugin '${r"${info.name()}"}' version '${r"${info.version()}"}' successfully loaded!")
    }

    @EventListener(ContextClosedEvent)
    final def onContextClosed() {
        final def info = VitalCoreModule.getRequiredAnnotation(MyGroovyPlugin, Vital.Info)
        logger.info("Groovy Vital plugin '${r"${info.name()}"}' version '${r"${info.version()}"}' successfully unloaded!")
    }
}
