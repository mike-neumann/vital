package me.myproject;

import dev.vitalframework.VitalPlugin;
import dev.vitalframework.VitalCoreModule;
import org.slf4j.Logger;

@VitalPlugin.Info(
        name = "${name}",
        description = "${description}",
        apiVersion = "${apiVersion}",
        version = "${version}",
        author = {${authors?map(it -> "\"" + it +  "\"")?join(", ")}}
)
public class MyJavaPlugin extends VitalPlugin.<#if pluginEnvironment == "SPIGOT">Spigot</#if><#if pluginEnvironment == "PAPER">Paper</#if><#if pluginEnvironment == "BUNGEE">Bungee</#if> {
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
