package me.vitalframework.players

import java.util.UUID

/**
 * Internal class; used to create and destroy custom instances of [VitalPlayer]
 * that are configured via `vital.players.player-class-name`.
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
