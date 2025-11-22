package me.vitalframework.cloudnet4.driver

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-cloudnet4-driver")
class VitalCloudNet4DriverSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("eu.cloudnetservice.driver.DriverEnvironment")
        } catch (_: Exception) {
            logger.error(
                "'vital-cloudnet4-driver' has been installed, but the CloudNet 4 driver was not found on the server classpath, calling CloudNet APIs might fail.",
            )
            logger.error("Please make sure you have the CloudNet 4 driver installed on your server.")
        }
    }
}
