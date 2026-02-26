package me.vitalframework.minigames

import me.vitalframework.VitalRepository
import java.util.UUID

/**
 * The global repository for in-memory data management for [VitalMinigameInstance] instances.
 */
open class VitalMinigameInstanceRepository : VitalRepository<VitalMinigameInstance, UUID>() {
    inline fun <reified T : VitalMinigameInstance> findByPlayerUniqueId(playerUniqueId: UUID): T? =
        entities
            .filterIsInstance<T>()
            .find { playerUniqueId in it.world.players.map { it.uniqueId } }
}
