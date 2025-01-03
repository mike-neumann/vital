package me.vitalframework.players

import java.util.*

class VitalPlayerService(
    val playerRepository: VitalPlayerRepository,
) {
    fun <T : VitalPlayer<*>> createPlayer(player: Any, playerUniqueId: UUID, playerType: Class<T>) {
        // Retrieve the VitalPlayer associated with the joining player, if it exists.
        val vitalPlayer = playerRepository.get(playerUniqueId)

        if (vitalPlayer != null) {
            return
        }

        // Create a new VitalPlayer for the joining player.
        try {
            val vitalPlayer = playerType.getDeclaredConstructor(playerType).newInstance(player)

            // Register the VitalPlayer with VitalUserManagement.
            playerRepository.save(vitalPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("error while creating vital player instance ${playerType.getSimpleName()} for $playerUniqueId")
        }
    }

    fun destroyPlayer(playerUniqueId: UUID) {
        // Retrieve the VitalPlayer associated with the leaving player.
        val vitalPlayer = playerRepository.get(playerUniqueId)
            ?: return

        // Unregister the VitalPlayer from VitalUserManagement.
        playerRepository.delete(vitalPlayer)
    }
}