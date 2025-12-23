package me.vitalframework.minigames

import me.vitalframework.SpigotListener
import me.vitalframework.SpigotPlugin
import me.vitalframework.tasks.VitalCountdownTask

/**
 * Defines a countdown minigame state within the Vital-Framework.
 * A countdown minigame state can be any state within a running or preparing minigame, which contains a countdown.
 * Every countdown state will by default be a [VitalMinigameState], [SpigotListener] and [VitalCountdownTask].
 *
 * Listener-events will be scoped to the currently active state.
 * Meaning the event-handlers defined within a single state will only trigger, when that state is active.
 * The current minigame state can be managed by [VitalMinigameService].
 *
 * ```java
 * @MinigameState
 * public class MyCountdownMinigameState extends VitalCountdownMinigameState {#
 *   public MyCountdownMinigameState(JavaPlugin plugin) {
 *     super(plugin);
 *   }
 * }
 * ```
 */
abstract class VitalCountdownMinigameState(
    plugin: SpigotPlugin,
) : VitalCountdownTask.Spigot(plugin),
    VitalMinigameState
