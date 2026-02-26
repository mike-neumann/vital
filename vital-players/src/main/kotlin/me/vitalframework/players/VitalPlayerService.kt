package me.vitalframework.players

import java.util.UUID

/**
 * The global custom player instance manager which can be used to create instances of custom [VitalPlayer] implementations.
 * This service should only rarely be of use to implementing plugins.
 * It is only used internally in Vital.
 */
class VitalPlayerService(
    private val vitalPlayerRepository: VitalPlayerRepository,
) {
    /**
     * Creates and saves a new instance of the specified custom [VitalPlayer] class.
     */
    fun <T : Any> createPlayer(
        player: T,
        playerUniqueId: UUID,
        playerClass: Class<T>,
        vitalPlayerClass: Class<out VitalPlayer<*>>,
    ) = try {
        if (vitalPlayerRepository.existsById(playerUniqueId)) {
            throw VitalPlayerException.AlreadyExists(
                playerClass,
                playerUniqueId,
            )
        }
        vitalPlayerRepository.save(
            vitalPlayerClass.getDeclaredConstructor(playerClass).newInstance(playerClass.cast(player)),
        )
    } catch (e: Exception) {
        throw VitalPlayerException.Create(vitalPlayerClass, playerUniqueId, e)
    }

    /**
     * Deletes the custom [VitalPlayer] instance from the global [VitalPlayerRepository] that matches the given [playerUniqueId].
     */
    fun deletePlayer(playerUniqueId: UUID) {
        val vitalPlayer = vitalPlayerRepository.findById<VitalPlayer<*>>(playerUniqueId) ?: return
        vitalPlayerRepository.delete(vitalPlayer)
    }
}
