package me.vitalframework.cloudnet4.bridge

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

/**
 * Defines the official vital-cloudnet4-bridge submodule, which is displayed when Vital starts.
 * It contains a wrapper around the CloudNet4-Bridge-API to interact with your CloudNet system in a developer-friendly way.
 */
@SubModule("vital-cloudnet4-bridge")
class VitalCloudNet4BridgeSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("eu.cloudnetservice.modules.bridge.BridgeDocProperties")
        } catch (_: Exception) {
            logger.error(
                "'vital-cloudnet4-bridge' has been installed, but the CloudNet 4 bridge was not found on the server classpath, calling CloudNet APIs might fail.",
            )
            logger.error("Please make sure you have the CloudNet 4 bridge installed on your server.")
        }
    }
}
