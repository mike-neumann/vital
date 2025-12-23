package me.vitalframework.processor

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule

/**
 * Defines the official vital-core-processor submodule, which is displayed when Vital starts.
 * This submodule should never be used in the final plugin jar.
 * It should only exist during project compilation.
 */
@SubModule("vital-core-processor")
class VitalCoreProcessorSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.error("'vital-core-processor' has been installed but this submodule should not be included in your plugin.")
        logger.error("You should exclude 'vital-core-processor' from your build as it is only relevant during compilation.")
    }
}
