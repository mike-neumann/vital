package me.vitalframework.holograms

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

/**
 * Defines the official vital-holograms submodule, which is displayed when Vital starts.
 * It contains the Vital holograms system, which can be used to create global and per-player based holograms.
 */
@SubModule("vital-holograms")
class VitalHologramsSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-holograms' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-holograms' in the correct server environment, e.g. Spigot, Paper.")
        }
    }
}
