package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import me.vitalframework.Vital.context
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.springframework.stereotype.Service

/**
 * Manages the current state of a minigame using the Vital framework.
 */
@Service
class VitalMinigameService(
    val plugin: SpigotPlugin,
) {
    private var vitalMinigameState: VitalBaseMinigameState? = null

    /**
     * Checks if the current minigame state matches a specified class.
     */
    fun <T : VitalBaseMinigameState> isMinigameState(type: Class<T>) =
        vitalMinigameState != null && type == (vitalMinigameState as VitalMinigameState).javaClass

    /**
     * Sets the current minigame state by Class.
     */
    fun <T : VitalBaseMinigameState> setMinigameState(type: Class<T>) {
        setMinigameState(context.getBean(type))
    }

    /**
     * Sets the current minigame state.
     * If a previous state exists, it is unregistered before registering the new state.
     */
    fun setMinigameState(minigameState: VitalBaseMinigameState) {
        if (this.vitalMinigameState != null) {
            if (this.vitalMinigameState is VitalCountdownMinigameState) {
                (vitalMinigameState as VitalCountdownMinigameState).stop()
            }

            // unregister listener from bukkit.
            HandlerList.unregisterAll(this.vitalMinigameState!!)
            this.vitalMinigameState!!.onDisable()
        }

        this.vitalMinigameState = minigameState
        Bukkit.getPluginManager().registerEvents(minigameState, plugin)
        minigameState.onEnable()
    }
}