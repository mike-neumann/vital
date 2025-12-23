package me.vitalframework.minigames

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

/**
 * Defines the official vital-minigames submodule, which is displayed when Vital starts.
 * It contains the Vital minigames system, which ban be used to create minigames.
 */
@SubModule("vital-minigames")
class VitalMinigamesSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-minigames' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-minigames' in the correct server environment, e.g. Spigot, Paper.")
        }
    }
}
