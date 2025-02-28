package me.vitalframework.players

import java.util.*

abstract class VitalPlayerException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause) {
    class InvalidClass(playerClass: Class<*>, cause: Throwable) :
        VitalPlayerException(
            "invalid class '${playerClass.simpleName}' does not extend '${VitalPlayer::class.java.simpleName}'",
            cause
        )

    class Create(playerClass: Class<*>, playerUniqueId: UUID, cause: Throwable) :
        VitalPlayerException(
            "error while creating vital player instance '${playerClass.simpleName}' for '$playerUniqueId'",
            cause
        )
}