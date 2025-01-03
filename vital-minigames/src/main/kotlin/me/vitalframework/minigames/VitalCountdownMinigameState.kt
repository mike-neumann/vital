package me.vitalframework.minigames

import me.vitalframework.tasks.VitalCountdownTask
import org.bukkit.plugin.java.JavaPlugin

/**
 * An abstract class for countdown-based [VitalBaseMinigameState] in the Vital framework.
 */
abstract class VitalCountdownMinigameState(
    plugin: JavaPlugin,
) : VitalCountdownTask.Spigot(plugin), VitalBaseMinigameState