package dev.vitalframework.players

import java.util.UUID

/**
 * Internal exception; thrown during [VitalPlayer] lifecycle.
 */
abstract class VitalPlayerException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown when the configured custom player class has an invalid signature.
     */
    class InvalidClass(
        playerClass: Class<*>,
        cause: Throwable,
    ) : VitalPlayerException(
            "Invalid Vital player class '${playerClass.simpleName}' does not extend '${VitalPlayer::class.java.simpleName}'",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurred during instantiation of the configured custom player class.
     */
    class Create(
        playerClass: Class<*>,
        playerUniqueId: UUID,
        cause: Throwable,
    ) : VitalPlayerException(
            "Error while creating Vital player instance '${playerClass.simpleName}' for '$playerUniqueId'",
            cause,
        )

    /**
     * Internal exception; thrown when an instance of a custom player class was created even though one already exists.
     */
    class AlreadyExists(
        playerClass: Class<*>,
        playerUniqueId: UUID,
    ) : VitalPlayerException(
            "Error while creating Vital player instance '${playerClass.simpleName}' for '$playerUniqueId', instance already exists",
        )

    /**
     * Internal exception; thrown when an exception occurs while checking the configured custom Vital player class.
     */
    class CheckPlayerClass(
        playerClassName: String,
        cause: Throwable,
    ) : VitalPlayerException(
            "Error while checking custom Vital player class '$playerClassName'. Does this class exist? Is it reachable by your plugin's classloader / Vital? Does the class extend VitalPlayer?",
            cause,
        )
}
