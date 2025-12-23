package me.vitalframework.minigames

import me.vitalframework.SpigotListener

/**
 * Defines a minigame state within the Vital-Framework.
 * A minigame state can be any state within a running or preparing minigame.
 * Every state will by default be a [SpigotListener].
 *
 * Listener-events will be scoped to the currently active state.
 * Meaning the event-handlers defined within a single state will only trigger, when that state is active.
 * The current minigame state can be managed by [VitalMinigameService].
 *
 * ```java
 * @MinigameState
 * public class MyMinigameState implements VitalMinigameState {
 *   @Override
 *   public void onEnable() {
 *     // ...
 *   }
 *
 *   @Override
 *   public void onDisable() {
 *     // ...
 *   }
 * }
 * ```
 */
interface VitalMinigameState : SpigotListener {
    /**
     * This method is invoked to enable the state of the minigame.
     * It is typically called when a new minigame state is set within the system, allowing
     * for any necessary setup or initialization specific to the activated minigame state.
     */
    fun onEnable() {}

    /**
     * Invoked when the minigame state is being disabled.
     * This method is typically used for cleanup operations and to release any resources
     * or listeners associated with the state before it is removed or replaced.
     */
    fun onDisable() {}
}
