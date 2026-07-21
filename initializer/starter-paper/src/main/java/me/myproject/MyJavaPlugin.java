package me.myproject;

import dev.vitalframework.Vital;
import dev.vitalframework.VitalCoreModule;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Vital.Info(
        name = "starter-paper",
        description = "Vital starter paper plugin",
        apiVersion = "1.21",
        version = "1.0.0",
        author = {"me"},
        environment = Vital.PluginEnvironment.PAPER
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
