package me.vitalframework.minigames

import org.bukkit.event.Listener

interface VitalBaseMinigameState : Listener {
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