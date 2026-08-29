package dev.vitalframework.cloudnet4.bridge

import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule

@VitalModule.Info(value = "vital-cloudnet4-bridge")
class VitalCloudNet4BridgeModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("eu.cloudnetservice.modules.bridge.BridgeDocProperties")
        } catch (_: Exception) {
            logger.error(
                "'vital-cloudnet4-bridge' has been enabled, but the CloudNet 4 bridge was not found on the server classpath, calling CloudNet APIs might fail.",
            )
            logger.error("Please make sure you have the CloudNet 4 bridge installed on your server.")
        }
    }
}
