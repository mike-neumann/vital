package dev.vitalframework.players

import dev.vitalframework.VitalCoreModule.Companion.logger
import java.util.UUID

/**
 * Internal class; used to create and destroy custom instances of [VitalPlayer]
 * that are configured via `vital.players.player-class-name`.
 */
class VitalPlayerService(
    private val vitalPlayerRepository: VitalPlayerRepository,
) {
    private val logger = logger()

    /**
     * Creates and saves a new instance of the specified custom [VitalPlayer] class.
     */
    fun <T : Any> createPlayer(
        player: T,
        playerUniqueId: UUID,
        playerClass: Class<T>,
        vitalPlayerClass: Class<out VitalPlayer<*>>,
    ): VitalPlayer<*> =
        try {
            val loggingContext =
                "Context: player '$player', player uuid '$playerUniqueId', " +
                    "player class '$playerClass', Vital player class '$vitalPlayerClass'."
            logger.debug("New Vital player instance construction was requested. $loggingContext")

            if (vitalPlayerRepository.existsById(playerUniqueId)) {
                throw VitalPlayerException.AlreadyExists(
                    playerClass,
                    playerUniqueId,
                )
            }

            val vitalPlayer = vitalPlayerClass.getDeclaredConstructor(playerClass).newInstance(playerClass.cast(player))
            logger.debug("New Vital player instance was constructed, '$vitalPlayer'. $loggingContext")
            vitalPlayerRepository.save(vitalPlayer)
        } catch (e: Exception) {
            throw VitalPlayerException.Create(vitalPlayerClass, playerUniqueId, e)
        }

    /**
     * Deletes the custom [VitalPlayer] instance from the global [VitalPlayerRepository] that matches the given [playerUniqueId].
     */
    fun deletePlayer(playerUniqueId: UUID) {
        logger.debug("Vital player instance deletion was requested for uuid '$playerUniqueId'.")
        val vitalPlayer = vitalPlayerRepository.findById<VitalPlayer<*>>(playerUniqueId)
        if (vitalPlayer == null) {
            logger.debug("Cannot delete Vital player instance for uuid '$playerUniqueId'. No Vital player instance found.")
            return
        }

        vitalPlayerRepository.delete(vitalPlayer)
        logger.debug("Vital player instance with uuid '$playerUniqueId' was deleted, '$vitalPlayer'.")
    }
}
