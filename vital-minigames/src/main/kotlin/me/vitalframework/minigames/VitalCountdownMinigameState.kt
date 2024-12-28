package me.vitalframework.minigames

import lombok.Getter
import me.vitalframework.tasks.VitalCountdownTask
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * An abstract class for countdown-based [VitalBaseMinigameState] in the Vital framework.
 */
@Getter
abstract class VitalCountdownMinigameState(
    plugin: JavaPlugin,
) : VitalCountdownTask.Spigot(plugin), VitalBaseMinigameState {
    override val uniqueId: UUID = UUID.randomUUID()
}