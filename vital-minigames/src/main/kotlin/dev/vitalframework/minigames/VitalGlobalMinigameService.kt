package dev.vitalframework.minigames

import dev.vitalframework.SpigotPlugin
import dev.vitalframework.tasks.VitalCountdownTask
import dev.vitalframework.tasks.VitalRepeatableTask
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList

/**
 * The global minigame manager to use when creating a global minigame for the entire game server.
 * A minigame consists of multiple states, which all are listeners by default.
 * Only the state that is currently active in this service will receive game-events.
 * Switching to another state means that the previous state will not be an active listener anymore.
 */
open class VitalGlobalMinigameService(
    val plugin: SpigotPlugin,
    val states: List<VitalGlobalMinigameState>,
) {
    /**
     * Holds the currently active state.
     * All event handlers defined on this state are active while this state is active.
     * Once switched to another state, all event handlers of the old state will be unregistered,
     * while all event handlers of the new state are registered.
     */
    var state: VitalGlobalMinigameState? = null
        private set

    /**
     * Checks if the given state is active by its class.
     */
    inline fun <reified T : VitalGlobalMinigameState> isStateActive() = isStateActive(T::class.java)

    /**
     * Checks if the given state is active by its class.
     */
    @PublishedApi
    internal fun <T : VitalGlobalMinigameState> isStateActive(type: Class<T>) = state != null && type == state!!.javaClass

    /**
     * Sets the state of the game to the state with the given class.
     * If a state with the given class cannot be found, an exception is thrown.
     *
     * When a state is switched, all event handlers of the previous state will be unregistered and become unresponsive to game events.
     * The new state will take over and receive new game events.
     *
     * If the previous state was a [VitalCountdownTask] or [VitalRepeatableTask], the respective [VitalCountdownTask.stop] and [VitalRepeatableTask.stop] functions are called.
     * Upon previous state unregistration, the [VitalGlobalMinigameState.onDisable] function is called, the new state's [VitalGlobalMinigameState.onEnable] function is called.
     */
    inline fun <reified T : VitalGlobalMinigameState> setState() = setState(T::class.java)

    /**
     * Sets the state of the game to the state with the given class.
     * If a state with the given class cannot be found, an exception is thrown.
     *
     * When a state is switched, all event handlers of the previous state will be unregistered and become unresponsive to game events.
     * The new state will take over and receive new game events.
     *
     * If the previous state was a [VitalCountdownTask] or [VitalRepeatableTask], the respective [VitalCountdownTask.stop] and [VitalRepeatableTask.stop] functions are called.
     * Upon previous state unregistration, the [VitalGlobalMinigameState.onDisable] function is called, the new state's [VitalGlobalMinigameState.onEnable] function is called.
     */
    @PublishedApi
    internal fun <T : VitalGlobalMinigameState> setState(type: Class<T>) = setState(states.find { it.javaClass == type }!!)

    /**
     * Sets the state of the game to the given state.
     *
     * When a state is switched, all event handlers of the previous state will be unregistered and become unresponsive to game events.
     * The new state will take over and receive new game events.
     *
     * If the previous state was a [VitalCountdownTask] or [VitalRepeatableTask], the respective [VitalCountdownTask.stop] and [VitalRepeatableTask.stop] functions are called.
     * Upon previous state unregistration, the [VitalGlobalMinigameState.onDisable] function is called, the new state's [VitalGlobalMinigameState.onEnable] function is called.
     */
    fun setState(state: VitalGlobalMinigameState) {
        if (this.state != null) {
            if (this.state is VitalCountdownTask<*, *, *>) {
                (this.state as VitalCountdownTask<*, *, *>).stop()
            }

            if (this.state is VitalRepeatableTask<*, *, *>) {
                (this.state as VitalRepeatableTask<*, *, *>).stop()
            }

            // unregister listener from bukkit.
            HandlerList.unregisterAll(this.state!!)
            this.state!!.onDisable()
        }

        this.state = state
        Bukkit.getPluginManager().registerEvents(state, plugin)
        state.onEnable()
    }
}
