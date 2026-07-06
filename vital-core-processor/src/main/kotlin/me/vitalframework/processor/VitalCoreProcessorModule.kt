package me.vitalframework.processor

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule

/**
 * Defines the official vital-core-processor module, which is displayed when Vital starts.
 * This module should never be used in the final plugin jar.
 * It should only exist during project compilation.
 */
@VitalModule.Info(value = "vital-core-processor")
class VitalCoreProcessorModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        logger.error("'vital-core-processor' has been enabled but this module should not be included in your plugin.")
        logger.error("You should exclude 'vital-core-processor' from your build as it is only relevant during compilation.")
    }
}
