package me.myproject;

import dev.vitalframework.VitalPlugin;
import dev.vitalframework.VitalCoreModule;
import org.slf4j.Logger;

@VitalPlugin.Info(
        name = "starter-bungee",
        description = "starter-bungee description",
        apiVersion = "1.21",
        version = "1.0.0",
        author = {"me"}
)
public class MyJavaPlugin extends VitalPlugin.Bungee {
    private final Logger logger = VitalCoreModule.logger(this);

    @Override
    public void onEnable() {
        final var info = VitalCoreModule.getRequiredAnnotation(MyJavaPlugin.class, VitalPlugin.Info.class);
        logger.info("Java Vital plugin '{}' version '{}' successfully loaded!", info.name(), info.version());
    }

    @Override
    public void onDisable() {
        final var info = VitalCoreModule.getRequiredAnnotation(MyJavaPlugin.class, VitalPlugin.Info.class);
        logger.info("Java Vital plugin '{}' version '{}' successfully unloaded!", info.name(), info.version());
    }
}
