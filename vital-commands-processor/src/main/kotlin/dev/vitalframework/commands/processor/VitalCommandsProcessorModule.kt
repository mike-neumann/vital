package dev.vitalframework.commands.processor

import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule

/**
 * Defines the official vital-commands-processor module, which is displayed when Vital starts.
 * This module should never be used in the final plugin jar.
 * It should only exist during project compilation.
 */
@VitalModule.Info(value = "vital-commands-processor")
class VitalCommandsProcessorModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        logger.error("'vital-commands-processor' has been enabled but this module should not be included in your plugin.")
        logger.error("You should exclude 'vital-commands-processor' from your build as it is only relevant during compilation.")
    }
}
