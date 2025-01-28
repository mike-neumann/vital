package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import me.vitalframework.tasks.VitalCountdownTask

/**
 * An abstract class for countdown-based [VitalBaseMinigameState] in the Vital framework.
 */
abstract class VitalCountdownMinigameState(
    plugin: SpigotPlugin,
) : VitalCountdownTask.Spigot(plugin), VitalBaseMinigameState