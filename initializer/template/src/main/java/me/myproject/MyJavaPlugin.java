package me.myproject;

import me.vitalframework.Vital;
import me.vitalframework.VitalCoreSubModule;
import org.slf4j.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Vital.Info(
        name = "${name}",
        description = "${description}",
        apiVersion = "${apiVersion}",
        version = "${version}",
        author = {${author}},
        environment = ${pluginEnvironment}
)
public class MyJavaPlugin {
    private final Logger logger = VitalCoreSubModule.logger(this);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        final var info = VitalCoreSubModule.getVitalInfo(MyJavaPlugin.class);
        logger.info("Java Vital plugin '{}' version '{}' successfully loaded!", info.name(), info.version());
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosed() {
        final var info = VitalCoreSubModule.getVitalInfo(MyJavaPlugin.class);
        logger.info("Java Vital plugin '{}' version '{}' successfully unloaded!", info.name(), info.version());
    }
}
