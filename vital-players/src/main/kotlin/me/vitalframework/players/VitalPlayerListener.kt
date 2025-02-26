package me.vitalframework.players

import me.vitalframework.*
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
interface VitalPlayerListener {
    val playerService: VitalPlayerService
    val playerClassName: String

    fun <T : Any> createPlayer(player: T, playerUniqueId: UUID) {
        @Suppress("UNCHECKED_CAST")
        playerService.createPlayer(player, playerUniqueId, Class.forName(playerClassName) as Class<out VitalPlayer<UUID>>)
    }

    fun destroyPlayer(playerUniqueId: UUID) {
        playerService.destroyPlayer(playerUniqueId)
    }

    @RequiresSpigot
    @Component
    class Spigot(
        plugin: SpigotPlugin,
        override val playerService: VitalPlayerService,
        @Value("\${vital.players.player-class-name:me.vitalframework.players.VitalPlayer.Spigot}")
        override val playerClassName: String,
    ) : VitalPlayerListener, VitalListener.Spigot(plugin) {
        // should always be executed first.
        @SpigotEventHandler(priority = SpigotEventPriority.LOWEST)
        fun onPlayerJoin(e: PlayerJoinEvent) {
            createPlayer(e.player, e.player.uniqueId)
        }

        // should always be executed last.
        @SpigotEventHandler(priority = SpigotEventPriority.HIGHEST)
        fun onPlayerQuit(e: PlayerQuitEvent) {
            destroyPlayer(e.player.uniqueId)
        }
    }

    @RequiresBungee
    @Component
    class Bungee(
        plugin: BungeePlugin,
        override val playerService: VitalPlayerService,
        @Value("\${vital.players.player-class-name:me.vitalframework.players.VitalPlayer.Bungee}")
        override val playerClassName: String,
    ) : VitalPlayerListener, VitalListener.Bungee(plugin) {
        // should always be executed first.
        @BungeeEventHandler(priority = BungeeEventPriority.LOWEST)
        fun onPostLogin(e: PostLoginEvent) {
            createPlayer(e.player, e.player.uniqueId)
        }

        // should always be executed last.
        @BungeeEventHandler(priority = BungeeEventPriority.HIGHEST)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
            destroyPlayer(e.player.uniqueId)
        }
    }
}