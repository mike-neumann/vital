package dev.vitalframework.minigames

import dev.vitalframework.tasks.VitalCountdownTask

/**
 * Defines a single game instance state within an active minigame instance, that is capable of running as a countdown task.
 * All event handlers registered here will only be active, when this state is active in its registered game instance.
 * A state may perform mutation on its own instance that's passed to its constructor to modify game data.
 *
 * ```java
 * @GlobalMinigameState
 * public class MyCountdownMinigameInstanceState extends VitalCountdownMinigameState<MyMinigameInstance> {
 *   public MyCountdownMinigameInstanceState(MyMinigameInstance instance) {
 *     super(instance);
 *   }
 * }
 * ```
 */
open class VitalMinigameInstanceCountdownState<T : VitalMinigameInstance>(
    override val instance: T,
) : VitalCountdownTask.Spigot(instance.plugin),
    VitalMinigameInstanceState<T>
