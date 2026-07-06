package me.myproject;

import me.vitalframework.Vital;
import me.vitalframework.VitalCoreModule;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Vital.Info(
        name = "${name}",
        description = "${description}",
        apiVersion = "${apiVersion}",
        version = "${version}",
        author = {${authors?map(it -> "\"" + it +  "\"")?join(", ")}},
        environment = Vital.PluginEnvironment.${pluginEnvironment}
)
public class MyJavaPlugin {
    private final Logger logger = VitalCoreModule.logger(this);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        final var info = VitalCoreModule.getRequiredAnnotation(MyJavaPlugin.class, Vital.Info.class);
        logger.info("Java Vital plugin '{}' version '{}' successfully loaded!", info.name(), info.version());
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        final var info = VitalCoreModule.getRequiredAnnotation(MyJavaPlugin.class, Vital.Info.class);
        logger.info("Java Vital plugin '{}' version '{}' successfully unloaded!", info.name(), info.version());
    }
}
