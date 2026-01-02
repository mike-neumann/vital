package me.vitalframework.players

import me.vitalframework.BungeeEventHandler
import me.vitalframework.BungeeEventPriority
import me.vitalframework.BungeePlayer
import me.vitalframework.Listener
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotEventPriority
import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalListener
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import java.util.UUID

/**
 * Listener interface for managing player-related events and operations within the Vital framework.
 *
 * This interface provides methods to associate players with `VitalPlayer` instances,
 * manage their lifecycle events, and facilitate player interactions within specific
 * environments (e.g., Spigot or Bungee). It relies on a `VitalPlayerService` for backend operations.
 */
interface VitalPlayerListener {
    /**
     * The service instance responsible for managing player-related operations
     * within the Vital framework. Provides access to player creation, destruction,
     * and retrieval functionalities.
     */
    val playerService: VitalPlayerService

    /**
     * Provides configuration properties related to vital players within the system.
     *
     * These properties are loaded from the application's configuration file (e.g., application.yml or application.properties)
     * and used for initializing and managing player-related configurations.
     */
    val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties

    /**
     * Creates a new player instance and associates it with a given unique ID and player class.
     *
     * This function uses reflection to instantiate a `VitalPlayer` class linked to the specified player.
     * If the `VitalPlayer` class is invalid or does not extend the required class, an exception is thrown.
     *
     * @param T The type of the player instance being created.
     * @param player The player instance to be associated with the `VitalPlayer`.
     * @param playerUniqueId The unique identifier of the player.
     * @param playerClass The class type of the player instance.
     * @throws VitalPlayerException.InvalidClass If the `VitalPlayer` class does not extend the required `VitalPlayer` type.
     */
    fun <T : Any> createPlayer(
        player: T,
        playerUniqueId: UUID,
        playerClass: Class<T>,
    ) = try {
        playerService.createPlayer(
            player,
            playerUniqueId,
            playerClass,
            vitalPlayersConfigurationProperties.playerClass,
        )
    } catch (e: ClassCastException) {
        throw VitalPlayerException.InvalidClass(vitalPlayersConfigurationProperties.playerClass, e)
    }

    /**
     * Destroys a player entity associated with the given unique identifier.
     *
     * This method delegates the removal operation to the `playerService`. If the player
     * does not exist, no action is performed.
     *
     * @param playerUniqueId The unique identifier of the player to be destroyed.
     */
    fun destroyPlayer(playerUniqueId: UUID) = playerService.destroyPlayer(playerUniqueId)

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiresSpigot
    @Listener
    class Spigot(
        override val playerService: VitalPlayerService,
        override val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
    ) : VitalListener.Spigot(),
        VitalPlayerListener {
        // should always be executed first.
        @SpigotEventHandler(priority = SpigotEventPriority.LOWEST)
        fun onPlayerJoin(e: PlayerJoinEvent) {
            createPlayer(e.player, e.player.uniqueId, SpigotPlayer::class.java)
        }

        // should always be executed last.
        @SpigotEventHandler(priority = SpigotEventPriority.HIGHEST)
        fun onPlayerQuit(e: PlayerQuitEvent) {
            destroyPlayer(e.player.uniqueId)
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiresBungee
    @Listener
    class Bungee(
        override val playerService: VitalPlayerService,
        override val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
    ) : VitalListener.Bungee(),
        VitalPlayerListener {
        // should always be executed first.
        @BungeeEventHandler(priority = BungeeEventPriority.LOWEST)
        fun onPostLogin(e: PostLoginEvent) {
            createPlayer(e.player, e.player.uniqueId, BungeePlayer::class.java)
        }

        // should always be executed last.
        @BungeeEventHandler(priority = BungeeEventPriority.HIGHEST)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
            destroyPlayer(e.player.uniqueId)
        }
    }
}
