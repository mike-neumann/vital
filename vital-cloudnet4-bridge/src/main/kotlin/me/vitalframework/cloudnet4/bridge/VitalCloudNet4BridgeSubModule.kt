package me.vitalframework.cloudnet4.bridge

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-cloudnet4-bridge")
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
