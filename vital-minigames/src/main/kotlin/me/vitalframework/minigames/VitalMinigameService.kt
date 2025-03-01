package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import me.vitalframework.Vital
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.springframework.stereotype.Service

@Service
class VitalMinigameService(val plugin: SpigotPlugin) {
    final var minigameState: VitalBaseMinigameState? = null
        private set

    fun <T : VitalBaseMinigameState> isMinigameState(type: Class<T>) =
        minigameState != null && type == (minigameState as VitalMinigameState).javaClass

    fun <T : VitalBaseMinigameState> setMinigameState(type: Class<T>) {
        setMinigameState(Vital.context.getBean(type))
    }

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