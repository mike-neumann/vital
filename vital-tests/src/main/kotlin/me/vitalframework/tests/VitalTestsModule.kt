package me.vitalframework.tests

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule

@VitalModule.Info(value = "vital-tests")
class VitalTestsModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        logger.error("'vital-tests' has been enabled but this module should not be included in your plugin.")
        logger.error("You should exclude 'vital-tests' from your build as it is only relevant during testing.")
    }
}
