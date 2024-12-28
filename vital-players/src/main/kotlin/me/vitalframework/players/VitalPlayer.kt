package me.vitalframework.players

import lombok.Getter
import lombok.RequiredArgsConstructor
import me.vitalframework.VitalComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import org.bukkit.entity.Player

/**
 * Represents a player as a VitalComponent, providing access to the player's data and functionality.
 *
 * @author xRa1ny
 */
@RequiredArgsConstructor
@Getter
abstract class VitalPlayer<T>(
    val player: T,
) : VitalComponent {
    override fun onRegistered() {
    }

    override fun onUnregistered() {
    }

    class Spigot(player: Player) : VitalPlayer<Player>(player) {
        override val uniqueId = player.uniqueId
        override val name = player.name
    }

    class Bungee(player: ProxiedPlayer) : VitalPlayer<ProxiedPlayer>(player) {
        override val uniqueId = player.uniqueId
        override val name = player.name
    }
}