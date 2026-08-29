package me.myproject

import dev.vitalframework.VitalPlugin
import dev.vitalframework.VitalCoreModule

@VitalPlugin.Info(
    name = "${name}",
    description = "${description}",
    apiVersion = "${apiVersion}",
    version = "${version}",
    author = [${authors?map(it -> "\"" + it +  "\"")?join(", ")}]
)
class MyGroovyPlugin extends VitalPlugin.<#if pluginEnvironment == "SPIGOT">Spigot</#if><#if pluginEnvironment == "PAPER">Paper</#if><#if pluginEnvironment == "BUNGEE">Bungee</#if> {
    private final def logger = VitalCoreModule.logger(this)

    @Override
    void onEnable() {
        final def info = VitalCoreModule.getRequiredAnnotation(MyGroovyPlugin, VitalPlugin.Info)
        logger.info("Groovy Vital plugin '${r"${info.name()}"}' version '${r"${info.version()}"}' successfully loaded!")
    }

    @Override
    void onDisable() {
        final def info = VitalCoreModule.getRequiredAnnotation(MyGroovyPlugin, VitalPlugin.Info)
        logger.info("Groovy Vital plugin '${r"${info.name()}"}' version '${r"${info.version()}"}' successfully unloaded!")
    }
}
