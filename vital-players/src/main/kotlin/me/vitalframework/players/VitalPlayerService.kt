package me.vitalframework.players

import org.springframework.stereotype.Service
import java.util.*

@Service
class VitalPlayerService(private val playerRepository: VitalPlayerRepository) {
    fun <T : Any> createPlayer(player: T, playerUniqueId: UUID, playerClass: Class<T>, vitalPlayerClass: Class<out VitalPlayer<*>>) = try {
        if (playerRepository.exists(playerUniqueId)) throw VitalPlayerException.AlreadyExists(playerClass, playerUniqueId)
        playerRepository.save(vitalPlayerClass.getDeclaredConstructor(playerClass).newInstance(playerClass.cast(player)))
    } catch (e: Exception) {
        throw VitalPlayerException.Create(vitalPlayerClass, playerUniqueId, e)
    }

    fun destroyPlayer(playerUniqueId: UUID) {
        val vitalPlayer = playerRepository.get(playerUniqueId) ?: return
        playerRepository.delete(vitalPlayer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : VitalPlayer<*>> getPlayers() = playerRepository.entities as List<T>

    @Suppress("UNCHECKED_CAST")
    fun <T : VitalPlayer<*>> getPlayer(id: UUID) = playerRepository.get(id) as T?
}