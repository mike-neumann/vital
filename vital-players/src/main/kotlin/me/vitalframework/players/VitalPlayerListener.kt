package me.vitalframework.players

import me.vitalframework.*
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
interface VitalPlayerListener {
    val playerService: VitalPlayerService
    val vitalPlayerClassName: String

    fun <T : Any> createPlayer(player: T, playerUniqueId: UUID, playerClass: Class<T>) = try {
        // suppress since we catch the exception anyway (but the ide won't shut up)
        @Suppress("UNCHECKED_CAST")
        playerService.createPlayer(player, playerUniqueId, playerClass, Class.forName(vitalPlayerClassName) as Class<out VitalPlayer<*>>)
    } catch (e: ClassCastException) {
        throw VitalPlayerException.InvalidClass(Class.forName(vitalPlayerClassName), e)
    }

    fun destroyPlayer(playerUniqueId: UUID) = playerService.destroyPlayer(playerUniqueId)

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiresSpigot
    @Component
    class Spigot(
        plugin: SpigotPlugin,
        override val playerService: VitalPlayerService,
        @Value("\${vital.players.player-class-name:me.vitalframework.players.VitalPlayer.Spigot}")
        override val vitalPlayerClassName: String,
    ) : VitalPlayerListener, VitalListener.Spigot(plugin) {
        // should always be executed first.
        @SpigotEventHandler(priority = SpigotEventPriority.LOWEST)
        fun onPlayerJoin(e: PlayerJoinEvent) = createPlayer(e.player, e.player.uniqueId, SpigotPlayer::class.java)

        // should always be executed last.
        @SpigotEventHandler(priority = SpigotEventPriority.HIGHEST)
        fun onPlayerQuit(e: PlayerQuitEvent) = destroyPlayer(e.player.uniqueId)
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiresBungee
    @Component
    class Bungee(
        plugin: BungeePlugin,
        override val playerService: VitalPlayerService,
        @Value("\${vital.players.player-class-name:me.vitalframework.players.VitalPlayer.Bungee}")
        override val vitalPlayerClassName: String,
    ) : VitalPlayerListener, VitalListener.Bungee(plugin) {
        // should always be executed first.
        @BungeeEventHandler(priority = BungeeEventPriority.LOWEST)
        fun onPostLogin(e: PostLoginEvent) = createPlayer(e.player, e.player.uniqueId, BungeePlayer::class.java)

        // should always be executed last.
        @BungeeEventHandler(priority = BungeeEventPriority.HIGHEST)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) = destroyPlayer(e.player.uniqueId)
    }
}