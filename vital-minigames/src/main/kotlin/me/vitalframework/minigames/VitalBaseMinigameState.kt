package me.vitalframework.minigames

import me.vitalframework.SpigotListener

interface VitalBaseMinigameState : SpigotListener {
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