package me.vitalframework.players

import me.vitalframework.*
import java.util.*

abstract class VitalPlayer<T>(val player: T) : VitalEntity<UUID> {
    abstract class Spigot(player: SpigotPlayer) : VitalPlayer<SpigotPlayer>(player) {
        override var id = player.uniqueId
    }

    abstract class Bungee(player: BungeePlayer) : VitalPlayer<BungeePlayer>(player) {
        override var id: UUID = player.uniqueId
    }
}