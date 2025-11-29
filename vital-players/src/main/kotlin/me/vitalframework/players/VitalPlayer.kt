package me.vitalframework.players

import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalEntity
import java.time.Instant
import java.util.UUID

/**
 * Represents a core player abstraction within the Vital framework.
 *
 * This class defines a base for player implementations that operate in different server environments.
 * Each instance of `VitalPlayer` is associated with a specific player type [T] and a unique identifier.
 *
 * @param T The specific type of the player instance being represented.
 * @property player The underlying player instance of type [T].
 * @constructor Creates an instance of `VitalPlayer` for the associated [player].
 */
abstract class VitalPlayer<T>(
    val player: T,
) : VitalEntity<UUID> {
    val joinedAt: Instant = Instant.now()

    /**
     * Represents a Spigot implementation of the `VitalPlayer` abstraction.
     *
     * This class is specifically designed for the Spigot server environment and provides
     * a concrete player representation in the Spigot context. Each instance of this class
     * is linked to a `SpigotPlayer` and its unique identifier.
     *
     * @constructor Creates an instance of `Spigot` using the provided [player] of type `SpigotPlayer`.
     * @param player The underlying Spigot player instance this class represents.
     */
    open class Spigot(
        player: SpigotPlayer,
    ) : VitalPlayer<SpigotPlayer>(player) {
        override var id = player.uniqueId
    }

    /**
     * Represents a Bungee-specific implementation of a player within the Vital framework.
     *
     * This class extends the generic `VitalPlayer` abstraction and adapts it for use with
     * BungeeCord environments. Each `Bungee` player instance is uniquely associated with
     * a `UUID` derived from the provided `BungeePlayer`.
     *
     * @param player The instance of the Bungee-specific player abstraction [BungeePlayer].
     */
    open class Bungee(
        player: BungeePlayer,
    ) : VitalPlayer<BungeePlayer>(player) {
        override var id: UUID = player.uniqueId
    }
}
