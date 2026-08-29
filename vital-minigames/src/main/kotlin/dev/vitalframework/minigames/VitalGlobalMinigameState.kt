package dev.vitalframework.minigames

import dev.vitalframework.SpigotListener
import dev.vitalframework.VitalCoreModule.Companion.logger

/**
 * Defines a minigame state within the Vital-Framework.
 * A minigame state can be any state within a running or preparing minigame.
 * Every state will by default be a [SpigotListener].
 *
 * Listener-events will be scoped to the currently active state.
 * Meaning the event-handlers defined within a single state will only trigger, when that state is active.
 * The current minigame state can be managed by [VitalGlobalMinigameService].
 *
 * ```java
 * @GlobalMinigameState
 * public class MyGlobalMinigameState implements VitalGlobalMinigameState {
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
interface VitalGlobalMinigameState : SpigotListener {
    val logger get() = logger()

    /**
     * Lifecycle function; called when this state is enabled via [VitalGlobalMinigameService.setState].
     */
    fun onEnable() {
        logger.debug("Lifecycle function 'onEnable' was not overridden for Vital global minigame state '$this'.")
    }

    /**
     * Lifecycle function; called when this state is disabled by switching to a new state using [VitalGlobalMinigameService.setState].
     */
    fun onDisable() {
        logger.debug("Lifecycle function 'onDisable' was not overridden for Vital global minigame state '$this'.")
    }
}
