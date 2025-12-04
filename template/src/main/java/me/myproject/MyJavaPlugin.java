package me.myproject;

import me.vitalframework.Vital;
import me.vitalframework.VitalCoreSubModule;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

// TODO: You can delete the whole "java" directory, if you don't want to use Java for your plugin.
@Vital.Info(
        name = "MyJavaPlugin",
        description = "MyJavaPluginDescription",
        apiVersion = "1.21",
        version = "1.0.0",
        author = {"Me"},
        // TODO: Change this to your actual plugin environment: SPIGOT, PAPER or BUNGEE
        environment = Vital.Info.PluginEnvironment.SPIGOT
)
public class MyJavaPlugin {
    private final Logger logger = VitalCoreSubModule.logger(this);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        final var info = VitalCoreSubModule.getVitalInfo(MyJavaPlugin.class);
        logger.info("Vital plugin '{}' version '{}' successfully loaded!", info.name(), info.version());
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        final var info = VitalCoreSubModule.getVitalInfo(MyJavaPlugin.class);
        logger.info("Vital plugin '{}' version '{}' successfully unloaded!", info.name(), info.version());
    }
}
