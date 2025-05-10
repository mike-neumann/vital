package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.springframework.stereotype.Service

/**
 * Service class responsible for managing the state transitions and lifecycle of minigames.
 *
 * This service handles activating, deactivating, and transitioning between different minigame states,
 * while ensuring proper event registration and cleanup processes. It works with a predefined list of
 * possible minigame states and interacts with the Spigot event system for managing state-specific events.
 *
 * @param plugin The Spigot plugin instance that owns this service.
 * @param minigameStates A list of available minigame states, each of which extends `VitalBaseMinigameState`.
 */
@Service
class VitalMinigameService(val plugin: SpigotPlugin, val minigameStates: List<VitalBaseMinigameState>) {
    /**
     * Represents the currently active state of the minigame.
     *
     * This property holds the active implementation of `VitalBaseMinigameState`, or `null`
     * if no minigame state is currently active. It is used to manage the lifecycle of the
     * minigame system by enabling and disabling specific states as required.
     *
     * The state can be updated only through internal mechanisms, ensuring that proper
     * cleanup and initialization routines are followed when transitioning between states.
     */
    final var minigameState: VitalBaseMinigameState? = null
        private set

    /**
     * Checks if the current minigame state is of the specified type.
     *
     * This method allows you to determine if the actively managed minigame state
     * matches the provided type `T`, which must extend `VitalBaseMinigameState`.
     *
     * @return `true` if the current minigame state exists and is of the specified type `T`,
     *         otherwise `false`.
     * @param T The type of the minigame state to check against.
     */
    final inline fun <reified T : VitalBaseMinigameState> isMinigameState() = isMinigameState(T::class.java)

    /**
     * Checks whether the current minigame state matches the specified type.
     *
     * @param type The class type of the minigame state to check against.
     * @return `true` if the current minigame state exists and matches the specified type, `false` otherwise.
     */
    @PublishedApi
    internal fun <T : VitalBaseMinigameState> isMinigameState(type: Class<T>) = minigameState != null && type == minigameState!!.javaClass

    /**
     * Sets the current minigame state to the specified type `T`.
     *
     * This method allows for transitioning the minigame system to a new state by enabling
     * the corresponding state instance from the predefined list of `minigameStates`. If a
     * previous minigame state is already active, this method will handle its cleanup by:
     * - Calling `onDisable` for the active state.
     * - Unregistering any event listeners associated with the previous state.
     *
     * Once the new state is set:
     * - It registers its event listeners.
     * - Its `onEnable` method is invoked for initialization-specific behavior.
     *
     * @param T The type of the minigame state to be set, which must be a subclass of `VitalBaseMinigameState`.
     * @throws IllegalStateException If the specified state type is not found in `minigameStates`.
     */
    final inline fun <reified T : VitalBaseMinigameState> setMinigameState() = setMinigameState(T::class.java)

    /**
     * Updates the current minigame state to the one associated with the specified type.
     * The new state is retrieved from the available `minigameStates` using the provided class type.
     *
     * @param T The type of the minigame state, which must extend `VitalBaseMinigameState`.
     * @param type The `Class` object representing the type of the desired minigame state.
     *             This is used to find the matching state in the `minigameStates` list.
     */
    @PublishedApi
    internal fun <T : VitalBaseMinigameState> setMinigameState(type: Class<T>) =
        setMinigameState(minigameStates.find { it.javaClass == type }!!)

    /**
     * Updates the current minigame state to the provided state.
     * Handles the necessary lifecycle transitions, including disabling the current state (if active),
     * unregistering its events, and enabling the new state with its corresponding event registration.
     *
     * @param minigameState The new minigame state to be set. It must extend `VitalBaseMinigameState`.
     */
    fun setMinigameState(minigameState: VitalBaseMinigameState) {
        if (this.minigameState != null) {
            if (this.minigameState is VitalCountdownMinigameState) {
                (this.minigameState as VitalCountdownMinigameState).stop()
            }
            // unregister listener from bukkit.
            HandlerList.unregisterAll(this.minigameState!!)
            this.minigameState!!.onDisable()
        }

        this.minigameState = minigameState
        Bukkit.getPluginManager().registerEvents(minigameState, plugin)
        minigameState.onEnable()
    }
}