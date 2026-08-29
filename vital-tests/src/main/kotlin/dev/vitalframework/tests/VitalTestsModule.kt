package dev.vitalframework.tests

import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule

@VitalModule.Info(value = "vital-tests")
class VitalTestsModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        logger.error("'vital-tests' has been enabled but this module should not be included in your plugin.")
        logger.error("You should exclude 'vital-tests' from your build as it is only relevant during testing.")
    }
}
