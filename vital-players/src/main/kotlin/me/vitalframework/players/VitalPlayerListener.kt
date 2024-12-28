package me.vitalframework.players

import me.vitalframework.*
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A listener class that manages VitalPlayer instances when players join and leave the server.
 *
 * @author xRa1ny
 */
interface VitalPlayerListener {
    val playerService: VitalPlayerService
    val playerType: Class<VitalPlayer<*>>

    @RequiresSpigot
    @Component
    abstract class Spigot(
        plugin: SpigotPlugin,
        override val playerService: VitalPlayerService,

        @Value("\${vital.players.type:me.vitalframework.players.VitalPlayer.Spigot}")
        override val playerType: Class<VitalPlayer<*>>,
    ) : VitalPlayerListener, VitalListener.Spigot(plugin) {
        // should always be executed first.
        @SpigotEventHandler(priority = SpigotEventPriority.LOW)
        fun onPlayerJoin(e: PlayerJoinEvent) {
            playerService.handlePlayerJoin(e.player, e.player.uniqueId, playerType)
        }

        // should always be executed last.
        @SpigotEventHandler(priority = SpigotEventPriority.HIGH)
        fun onPlayerQuit(e: PlayerQuitEvent) {
            playerService.handlePlayerQuit(e.player.uniqueId)
        }
    }

    @RequiresBungee
    @Component
    abstract class Bungee(
        plugin: BungeePlugin,
        override val playerService: VitalPlayerService,

        @Value("\${vital.players.type:me.vitalframework.players.VitalPlayer.Bungee}")
        override val playerType: Class<VitalPlayer<*>>,
    ) : VitalPlayerListener, VitalListener.Bungee(plugin) {
        // should always be executed first.
        @BungeeEventHandler(priority = BungeeEventPriority.LOW)
        fun onPostLogin(e: PostLoginEvent) {
            playerService.handlePlayerJoin(e.player, e.player.uniqueId, playerType)
        }

        // should always be executed last.
        @BungeeEventHandler(priority = BungeeEventPriority.HIGH)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
            playerService.handlePlayerQuit(e.player.uniqueId)
        }
    }
}