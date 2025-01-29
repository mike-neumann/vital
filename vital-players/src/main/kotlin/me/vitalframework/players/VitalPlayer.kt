package me.vitalframework.players

import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalEntity
import java.util.*

/**
 * Represents a player as a VitalComponent, providing access to the player's data and functionality.
 */
abstract class VitalPlayer<T>(
    val player: T,
) : VitalEntity<UUID> {
    class Spigot(player: SpigotPlayer) : VitalPlayer<SpigotPlayer>(player) {
        override var id = player.uniqueId
    }

    class Bungee(player: BungeePlayer) : VitalPlayer<BungeePlayer>(player) {
        override var id: UUID = player.uniqueId
    }
}