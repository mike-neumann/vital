package me.vitalframework.commands.processor

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-commands-processor")
class VitalCommandsProcessorSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.error("'vital-commands-processor' has been installed but this submodule should not be included in your plugin.")
        logger.error("You should exclude 'vital-commands-processor' from your build as it is only relevant during compilation.")
    }
}
