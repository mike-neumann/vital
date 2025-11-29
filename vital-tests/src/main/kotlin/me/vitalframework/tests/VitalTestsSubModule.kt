package me.vitalframework.tests

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-tests")
class VitalTestsSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.error("'vital-tests' has been installed but this submodule should not be included in your plugin.")
        logger.error("You should exclude 'vital-tests' from your build as it is only relevant during testing.")
    }
}
