package me.vitalframework.scoreboards

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-scoreboards")
class VitalScoreboardsSubModule(
    val vitalScoreboards: List<VitalScoreboard>,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-scoreboards' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-scoreboards' in the correct server environment, e.g. Spigot, Paper.")
        }

        for (vitalScoreboard in vitalScoreboards) {
            logger.info("Scoreboard '${vitalScoreboard::class.java.name}' successfully registered")
        }

        logger.info("Please take note that only globally registered scoreboards are shown here.")
        logger.info("If your scoreboard is not displayed here, you likely haven't exposed it as a bean.")
    }
}
