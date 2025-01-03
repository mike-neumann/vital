package me.vitalframework.players

import lombok.Getter
import lombok.RequiredArgsConstructor
import me.vitalframework.VitalEntity
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * Represents a player as a VitalComponent, providing access to the player's data and functionality.
 */
@RequiredArgsConstructor
@Getter
abstract class VitalPlayer<T>(
    val player: T,
) : VitalEntity<UUID> {
    class Spigot(player: Player) : VitalPlayer<Player>(player) {
        override var id = player.uniqueId
    }

    class Bungee(player: ProxiedPlayer) : VitalPlayer<ProxiedPlayer>(player) {
        override var id: UUID = player.uniqueId
    }
}