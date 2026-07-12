package me.vitalframework.players

import me.vitalframework.BungeeEventHandler
import me.vitalframework.BungeeEventPriority
import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotEventPriority
import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalListener
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID

/**
 * Internal listener; manages [VitalPlayer] lifecycles.
 */
interface VitalPlayerListener {
    val vitalPlayerService: VitalPlayerService
    val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties

    /**
     * Internal function; creates a new [VitalPlayer] instance for the given [player] and stores it in [VitalPlayerRepository].
     * This function throws [VitalPlayerException.InvalidClass] if the configured custom player class has an invalid signature.
     */
    fun <T : Any> createPlayer(
        player: T,
        playerUniqueId: UUID,
        playerClass: Class<T>,
    ): VitalPlayer<*> =
        try {
            vitalPlayerService.createPlayer(
                player,
                playerUniqueId,
                playerClass,
                vitalPlayersConfigurationProperties.playerClass,
            )
        } catch (e: ClassCastException) {
            throw VitalPlayerException.InvalidClass(vitalPlayersConfigurationProperties.playerClass, e)
        }

    /**
     * Destroys the [VitalPlayer] instance of the given [playerUniqueId].
     * If no [VitalPlayer] instance exists for the given [playerUniqueId], this function does nothing.
     */
    fun destroyPlayer(playerUniqueId: UUID) = vitalPlayerService.deletePlayer(playerUniqueId)

    class Spigot(
        override val vitalPlayerService: VitalPlayerService,
        override val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
    ) : VitalListener.Spigot(),
        VitalPlayerListener {
        // should always be executed first.
        @SpigotEventHandler(SpigotEventPriority.LOWEST)
        fun onPlayerJoin(e: PlayerJoinEvent) {
            createPlayer(e.player, e.player.uniqueId, SpigotPlayer::class.java)
        }

        // should always be executed last.
        @SpigotEventHandler(SpigotEventPriority.HIGHEST)
        fun onPlayerQuit(e: PlayerQuitEvent) {
            // before destroying the vital player instance, we must first close the active inventory.
            e.player.closeInventory(InventoryCloseEvent.Reason.DISCONNECT)
            destroyPlayer(e.player.uniqueId)
        }
    }

    class Bungee(
        override val vitalPlayerService: VitalPlayerService,
        override val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
    ) : VitalListener.Bungee(),
        VitalPlayerListener {
        // should always be executed first.
        @BungeeEventHandler(BungeeEventPriority.LOWEST)
        fun onPostLogin(e: PostLoginEvent) {
            createPlayer(e.player, e.player.uniqueId, BungeePlayer::class.java)
        }

        // should always be executed last.
        @BungeeEventHandler(BungeeEventPriority.HIGHEST)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
            destroyPlayer(e.player.uniqueId)
        }
    }
}
