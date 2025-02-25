package me.vitalframework.players

import me.vitalframework.*
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

interface VitalPlayerListener {
    val playerService: VitalPlayerService
    val playerClassName: String

    @RequiresSpigot
    @Component
    class Spigot(
        plugin: SpigotPlugin,
        override val playerService: VitalPlayerService,
        @Value("\${vital.players.type:me.vitalframework.players.VitalPlayer.Spigot}")
        override val playerClassName: String,
    ) : VitalPlayerListener, VitalListener.Spigot(plugin) {
        // should always be executed first.
        @SpigotEventHandler(priority = SpigotEventPriority.LOWEST)
        fun onPlayerJoin(e: PlayerJoinEvent) {
            playerService.createPlayer(
                e.player,
                e.player.uniqueId,
                Class.forName(playerClassName) as Class<out VitalPlayer<*>>
            )
        }

        // should always be executed last.
        @SpigotEventHandler(priority = SpigotEventPriority.HIGHEST)
        fun onPlayerQuit(e: PlayerQuitEvent) {
            playerService.destroyPlayer(e.player.uniqueId)
        }
    }

    @RequiresBungee
    @Component
    class Bungee(
        plugin: BungeePlugin,
        override val playerService: VitalPlayerService,
        @Value("\${vital.players.type:me.vitalframework.players.VitalPlayer.Bungee}")
        override val playerClassName: String,
    ) : VitalPlayerListener, VitalListener.Bungee(plugin) {
        // should always be executed first.
        @BungeeEventHandler(priority = BungeeEventPriority.LOWEST)
        fun onPostLogin(e: PostLoginEvent) {
            playerService.createPlayer(
                e.player,
                e.player.uniqueId,
                Class.forName(playerClassName) as Class<out VitalPlayer<*>>
            )
        }

        // should always be executed last.
        @BungeeEventHandler(priority = BungeeEventPriority.HIGHEST)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
            playerService.destroyPlayer(e.player.uniqueId)
        }
    }
}