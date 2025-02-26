package me.vitalframework.players

import org.springframework.stereotype.Service
import java.util.*

@Service
class VitalPlayerService(private val playerRepository: VitalPlayerRepository) {
    fun <T : VitalPlayer<*>> createPlayer(player: Any, playerUniqueId: UUID, playerClass: Class<T>) {
        // Retrieve the VitalPlayer associated with the joining player, if it exists.
        playerRepository.get(playerUniqueId)?.let { return }
        // Create a new VitalPlayer for the joining player.
        try {
            // Register the VitalPlayer with VitalUserManagement.
            playerRepository.save(playerClass.getDeclaredConstructor(player.javaClass).newInstance(player))
        } catch (e: Exception) {
            throw VitalPlayerException.Create(playerClass, playerUniqueId, e)
        }
    }

    fun destroyPlayer(playerUniqueId: UUID) {
        // Retrieve the VitalPlayer associated with the leaving player.
        val vitalPlayer = playerRepository.get(playerUniqueId)
            ?: return
        // Unregister the VitalPlayer from VitalUserManagement.
        playerRepository.delete(vitalPlayer)
    }

    // TODO: playerRepository.entities returns null when running paper???????? -> should be impossible
    fun <T : VitalPlayer<*>> getPlayers() = playerRepository.entities as List<T>
    fun <T : VitalPlayer<*>> getPlayer(id: UUID) = playerRepository.get(id) as T?
}