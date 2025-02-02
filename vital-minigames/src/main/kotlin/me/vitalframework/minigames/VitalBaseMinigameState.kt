package me.vitalframework.minigames

import me.vitalframework.SpigotListener

interface VitalBaseMinigameState : SpigotListener {
    fun onEnable() {}
    fun onDisable() {}
}