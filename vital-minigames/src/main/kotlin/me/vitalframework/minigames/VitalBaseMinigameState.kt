package me.vitalframework.minigames

import me.vitalframework.VitalComponent
import org.bukkit.event.Listener

interface VitalBaseMinigameState : VitalComponent, Listener {
    /**
     * Called when this state is enabled.
     */
    fun onEnable() {
    }

    /**
     * Called when this state is disabled.
     */
    fun onDisable() {
    }
}