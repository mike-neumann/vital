package me.vitalframework.cloudnet4.driver

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule

@VitalModule.Info(value = "vital-cloudnet4-driver")
class VitalCloudNet4DriverModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("eu.cloudnetservice.driver.DriverEnvironment")
        } catch (_: Exception) {
            logger.error(
                "'vital-cloudnet4-driver' has been enabled, but the CloudNet 4 driver was not found on the server classpath, calling CloudNet APIs might fail.",
            )
            logger.error("Please make sure you have the CloudNet 4 driver installed on your server.")
        }
    }
}
