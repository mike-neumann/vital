package me.vitalframework.scoreboards

import me.vitalframework.RequiresSpigot
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.springframework.context.annotation.Conditional

@Conditional(RequiresSpigot::class)
@VitalModule.Info(value = "vital-scoreboards")
class VitalScoreboardsModule(
    val vitalScoreboards: List<VitalScoreboard>,
) : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-scoreboards' has been enabled, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-scoreboards' in the correct server environment, e.g. Spigot, Paper.")
        }

        for (vitalScoreboard in vitalScoreboards) {
            logger.info("Scoreboard '${vitalScoreboard::class.java.name}' successfully registered")
        }

        if (vitalScoreboards.isNotEmpty()) {
            logger.info("Please take note that only globally registered scoreboards are shown here.")
            logger.info("If your scoreboard is not displayed here, you likely haven't exposed it as a bean.")
        }
    }
}
