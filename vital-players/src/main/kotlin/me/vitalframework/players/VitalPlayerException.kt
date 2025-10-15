package me.vitalframework.players

import java.util.UUID

abstract class VitalPlayerException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class InvalidClass(
        playerClass: Class<*>,
        cause: Throwable,
    ) : VitalPlayerException(
            "invalid Vital player class '${playerClass.simpleName}' does not extend '${VitalPlayer::class.java.simpleName}'",
            cause,
        )

    class Create(
        playerClass: Class<*>,
        playerUniqueId: UUID,
        cause: Throwable,
    ) : VitalPlayerException(
            "error while creating Vital player instance '${playerClass.simpleName}' for '$playerUniqueId'",
            cause,
        )

    class AlreadyExists(
        playerClass: Class<*>,
        playerUniqueId: UUID,
    ) : VitalPlayerException(
            "error while creating Vital player instance '${playerClass.simpleName}' for '$playerUniqueId', instance already exists",
        )
}
