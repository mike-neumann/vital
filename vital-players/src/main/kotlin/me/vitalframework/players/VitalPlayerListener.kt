package me.vitalframework.players

import me.vitalframework.BungeeEventHandler
import me.vitalframework.BungeeEventPriority
import me.vitalframework.BungeePlayer
import me.vitalframework.BungeePlugin
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotEventPriority
import me.vitalframework.SpigotPlayer
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalListener
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Listener interface for managing player-related events and operations within the Vital framework.
 *
 * This interface provides methods to associate players with `VitalPlayer` instances,
 * manage their lifecycle events, and facilitate player interactions within specific
 * environments (e.g., Spigot or Bungee). It relies on a `VitalPlayerService` for backend operations.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
interface VitalPlayerListener {
    /**
     * The service instance responsible for managing player-related operations
     * within the Vital framework. Provides access to player creation, destruction,
     * and retrieval functionalities.
     */
    val playerService: VitalPlayerService

    /**
     * The fully qualified name of the class that represents a specific implementation of `VitalPlayer`.
     *
     * This variable is used to dynamically load and instantiate the appropriate `VitalPlayer`
     * subclass during runtime, based on its string representation of the class name.
     *
     * Its value must denote a class that extends `VitalPlayer` and adheres to its functionality.
     * Improper or invalid class assignments may lead to runtime exceptions, such as `ClassCastException`
     * or `VitalPlayerException.InvalidClass`.
     */
    val vitalPlayerClassName: String

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
        // suppress since we catch the exception anyway (but the ide won't shut up)
        @Suppress("UNCHECKED_CAST")
        playerService.createPlayer(
            player,
            playerUniqueId,
            playerClass,
            Class.forName(vitalPlayerClassName) as Class<out VitalPlayer<*>>,
        )
    } catch (e: ClassCastException) {
        throw VitalPlayerException.InvalidClass(Class.forName(vitalPlayerClassName), e)
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

    /**
     * Represents a Spigot-specific implementation of the `VitalListener` interface and supports
     * player-related event management within the Spigot server environment.
     *
     * This class listens for specific Spigot events such as player joining and quitting,
     * and links these events to corresponding functionality in the `VitalPlayerService` for
     * player creation and destruction.
     *
     * Feature Summary:
     * - Creates a new `VitalPlayer` instance when a player joins the server.
     * - Removes the `VitalPlayer` instance when a player disconnects.
     * - Uses reflection to dynamically resolve the `VitalPlayer` class for Spigot compatibility.
     *
     * Primary Responsibilities:
     * 1. Ensures that all player-related operations are synchronized with the `VitalPlayerService`.
     * 2. Responds to Spigot platform-specific events using annotated event handlers.
     *
     * Annotations:
     * - `@RequiresSpigot`: Indicates dependency on Spigot's API and ensures activation only within a Spigot environment.
     * - `@SpigotEventHandler`: Registers methods as listeners to Spigot-specific events with defined execution priority.
     * - `@Order`: Specifies the highest precedence for the bean ordering.
     *
     * @property playerService The service responsible for managing `VitalPlayer` instances.
     * @property vitalPlayerClassName The fully qualified class name of the `VitalPlayer` implementation.
     */
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiresSpigot
    @Component
    class Spigot(
        plugin: SpigotPlugin,
        override val playerService: VitalPlayerService,
        @param:Value($$"${vital.players.player-class-name:me.vitalframework.players.VitalPlayer$Spigot}")
        override val vitalPlayerClassName: String,
    ) : VitalListener.Spigot(plugin),
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

    /**
     * A Bungee-specific implementation of `VitalPlayerListener` and `BungeeListener`. This component integrates
     * with the BungeeCord platform to manage lifecycle events of players in the context of a Vital framework.
     * The Bungee class listens and reacts to specific player events like login and disconnection.
     *
     * This class ensures proper management of player entities with the following functionalities:
     * - Creation of `VitalPlayer` instances upon player login.
     * - Destruction of `VitalPlayer` instances upon player disconnection.
     *
     * The component is marked with `@Order` to set its execution precedence, ensuring that event handlers
     * are executed in the correct order relative to other listeners. It also uses `@RequiresBungee` to
     * validate the presence of the BungeeCord API before initialization.
     *
     * @constructor Creates a new Bungee instance.
     * @param plugin The Bungee plugin instance associated with this listener.
     * @param playerService The service used for managing `VitalPlayer` entities.
     * @param vitalPlayerClassName The fully qualified class name of the `VitalPlayer` implementation
     * specific to BungeeCord.
     *
     * @see VitalPlayerListener
     * @see VitalListener.Bungee
     */
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RequiresBungee
    @Component
    class Bungee(
        plugin: BungeePlugin,
        override val playerService: VitalPlayerService,
        @param:Value($$"${vital.players.player-class-name:me.vitalframework.players.VitalPlayer$Bungee}")
        override val vitalPlayerClassName: String,
    ) : VitalListener.Bungee(plugin),
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
