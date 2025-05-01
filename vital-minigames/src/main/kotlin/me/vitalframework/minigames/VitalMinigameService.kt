package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.springframework.stereotype.Service

@Service
class VitalMinigameService(val plugin: SpigotPlugin, val minigameStates: List<VitalBaseMinigameState>) {
    final var minigameState: VitalBaseMinigameState? = null
        private set

    final inline fun <reified T : VitalBaseMinigameState> isMinigameState() = isMinigameState(T::class.java)

    @PublishedApi
    internal fun <T : VitalBaseMinigameState> isMinigameState(type: Class<T>) = minigameState != null && type == minigameState!!.javaClass

    final inline fun <reified T : VitalBaseMinigameState> setMinigameState() = setMinigameState(T::class.java)

    @PublishedApi
    internal fun <T : VitalBaseMinigameState> setMinigameState(type: Class<T>) =
        setMinigameState(minigameStates.find { it.javaClass == type }!!)

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