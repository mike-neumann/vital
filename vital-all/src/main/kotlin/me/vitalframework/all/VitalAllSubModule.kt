package me.vitalframework.all

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/**
 * Defines the official vital-all submodule, which is displayed when Vital starts.
 * Including this submodule in your plugin will enable you to use all of Vital's functionalities.
 * It also includes submodules, which may not work for your platform.
 * You need to exclude those dependencies manually.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@SubModule("vital-all")
class VitalAllSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.warn(
            "'vital-all' has been installed, this includes ALL Vital submodules, even ones that might not support your current server's runtime",
        )
    }
}
