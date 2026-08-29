package dev.vitalframework.players

import dev.vitalframework.BungeePlayer
import dev.vitalframework.SpigotPlayer
import dev.vitalframework.VitalEntity
import java.time.Instant
import java.util.UUID

/**
 * Base custom player class for every platform implementation of `vital-players`.
 * Developers should very rarely use this class manually and rather use [Spigot] or [Bungee] instead.
 */
abstract class VitalPlayer<T>(
    val player: T,
) : VitalEntity<UUID> {
    /**
     * The [Instant] at which this player object was created (when the player joined).
     */
    val joinedAt: Instant = Instant.now()

    open class Spigot(
        player: SpigotPlayer,
    ) : VitalPlayer<SpigotPlayer>(player) {
        override var id = player.uniqueId
    }

    open class Bungee(
        player: BungeePlayer,
    ) : VitalPlayer<BungeePlayer>(player) {
        override var id: UUID = player.uniqueId
    }
}
