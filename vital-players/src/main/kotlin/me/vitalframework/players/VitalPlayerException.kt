package me.vitalframework.players

import java.util.*

abstract class VitalPlayerException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    class Create(playerClass: Class<*>, playerUniqueId: UUID) :
        VitalPlayerException("error while creating vital player instance '${playerClass.getSimpleName()}' for '$playerUniqueId'")
}