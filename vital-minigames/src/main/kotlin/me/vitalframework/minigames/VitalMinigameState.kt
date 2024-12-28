package me.vitalframework.minigames

import java.util.*

class VitalMinigameState : VitalBaseMinigameState {
    override val uniqueId: UUID = UUID.randomUUID()
    override val name: String = javaClass.getSimpleName()

    override fun onRegistered() {

    }

    override fun onUnregistered() {

    }
}