package me.vitalframework.players

import org.springframework.stereotype.Service
import java.util.*

@Service
class VitalPlayerService(private val playerRepository: VitalPlayerRepository) {
    fun <T : Any> createPlayer(player: T, playerUniqueId: UUID, playerClass: Class<T>, vitalPlayerClass: Class<out VitalPlayer<*>>) = try {
        // Retrieve the VitalPlayer associated with the joining player, if it exists.
        playerRepository.get(playerUniqueId)?.let { return }
        // Register the VitalPlayer with VitalUserManagement.
        playerRepository.save(vitalPlayerClass.getDeclaredConstructor(playerClass).newInstance(playerClass.cast(player)))
    } catch (e: Exception) {
        throw VitalPlayerException.Create(vitalPlayerClass, playerUniqueId, e)
    }

    fun destroyPlayer(playerUniqueId: UUID) {
        // Retrieve the VitalPlayer associated with the leaving player.
        val vitalPlayer = playerRepository.get(playerUniqueId) ?: return
        // Unregister the VitalPlayer from VitalUserManagement.
        playerRepository.delete(vitalPlayer)
    }

    // TODO: playerRepository.entities returns null when running paper???????? -> should be impossible
    fun <T : VitalPlayer<*>> getPlayers() = playerRepository.entities as List<T>
    fun <T : VitalPlayer<*>> getPlayer(id: UUID) = playerRepository.get(id) as T?
}