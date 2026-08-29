package me.myproject

import dev.vitalframework.VitalPlugin
import dev.vitalframework.VitalCoreModule.Companion.getVitalInfo
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation

@VitalPlugin.Info(
    "${name}",
    "${description}",
    "${apiVersion}",
    "${version}",
    [${authors?map(it -> "\"" + it +  "\"")?join(", ")}]
)
class MyKotlinPlugin : VitalPlugin.<#if pluginEnvironment == "SPIGOT">Spigot()</#if><#if pluginEnvironment == "PAPER">Paper()</#if><#if pluginEnvironment == "BUNGEE">Bungee()</#if> {
    private val logger = logger()

    override fun onEnable() {
        val info = MyKotlinPlugin::class.getRequiredAnnotation<VitalPlugin.Info>()
        logger.info("Kotlin Vital plugin '${r"${info.name}"}' version '${r"${info.version}"}' successfully loaded!")
    }

    override fun onDisable() {
        val info = MyKotlinPlugin::class.getRequiredAnnotation<VitalPlugin.Info>()
        logger.info("Kotlin Vital plugin '${r"${info.name}"}' version '${r"${info.version}"}' successfully unloaded!")
    }
}
