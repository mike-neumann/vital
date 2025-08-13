package me.vitalframework.minigames

import me.vitalframework.SpigotListener

/**
 * Represents a base interface for defining the state of a minigame.
 * Implementations of this interface represent various states that a minigame
 * can be in, ranging from pre-game setup to active gameplay or the post-game sequence.
 * Each state can register and unregister event listeners as well as perform
 * custom initialization or cleanup operations when transitioning between states.
 */
interface VitalBaseMinigameState : SpigotListener {
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
