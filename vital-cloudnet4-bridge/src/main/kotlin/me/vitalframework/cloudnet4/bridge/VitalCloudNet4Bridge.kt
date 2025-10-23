package me.vitalframework.cloudnet4.bridge

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.registry.ServiceRegistry
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.modules.bridge.BridgeDocProperties
import eu.cloudnetservice.modules.bridge.player.PlayerManager
import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotPlayer
import me.vitalframework.cloudnet4.driver.VitalCloudNet4Driver
import java.util.UUID

/**
 * Represents a bridge interface for managing player and server interactions within CloudNet4.
 * This interface defines methods for interacting with players, executing commands,
 * connecting to servers, retrieving server information, and counting players.
 *
 * @param P The type representing the player object.
 */
interface VitalCloudNet4Bridge<P> {
    companion object {
        /**
         * Singleton instance of the service registry used for managing and resolving dependencies
         * within the CloudNet4Bridge module. It provides a central access point for service instances,
         * allowing the injection and retrieval of configured services.
         *
         * This registry leverages an injection layer to resolve the `ServiceRegistry` instance.
         * It ensures that all required services are initialized and available for use across
         * the application where dependency management is required.
         */
        @JvmStatic
        val serviceRegistry get() = InjectionLayer.ext().instance(ServiceRegistry::class.java)!!

        /**
         * A globally accessible instance of the `PlayerManager` interface used to manage and interact with player-related
         * functionalities in the CloudNet framework. It is fetched from the `ServiceRegistry`.
         *
         * This instance facilitates operations such as retrieving player-specific information, managing player connections,
         * executing commands on players, and other cloud-integrated player management tasks.
         *
         * The value is guaranteed to be non-null. In case no provider is found for the `PlayerManager` service, it will
         * result in an exception being thrown during initialization.
         */
        @JvmStatic
        val playerManager get() = serviceRegistry.defaultInstance(PlayerManager::class.java)!!
    }

    /**
     * Retrieves the unique identifier (UUID) of the given player.
     *
     * @param player the player whose unique identifier is to be retrieved
     * @return the UUID of the specified player
     */
    fun getPlayerUniqueId(player: P): UUID

    /**
     * Gets the player executor instance associated with the current player.
     *
     * This function uses the player's unique identifier to retrieve their corresponding
     * executor through the player management system. The executor allows execution
     * of player-specific operations, such as sending commands or connecting to servers.
     *
     * @receiver The player instance for which the executor is being retrieved.
     * @return The player executor associated with the player.
     */
    fun P.getPlayerExecutor() = playerManager.playerExecutor(getPlayerUniqueId(this))

    /**
     * Connects the current player to a specified server.
     *
     * This method uses the player's executor to initiate a connection to the given server.
     * The server name must correspond to a valid, accessible server within the network.
     *
     * @receiver The player instance that will be connected to the server.
     * @param serverName The name of the server the player should connect to.
     */
    fun P.connect(serverName: String) = getPlayerExecutor().connect(serverName)

    /**
     * Executes the specified command on behalf of the player.
     *
     * This method uses the player's executor to perform the specified command
     * within the context of the server or game environment.
     *
     * @receiver The player instance on whose behalf the command is being executed.
     * @param command The command to be executed, provided as a string.
     */
    fun P.runCommand(command: String) = getPlayerExecutor().spoofCommandExecution(command)

    /**
     * Retrieves the first cloud server where the current player is present.
     *
     * This function filters through all running cloud servers using the specified criteria:
     * - The server contains the current player, identified by their unique identifier, in its player list.
     *
     * If no server matches the criteria, the function returns null.
     *
     * @receiver The player instance for whom the cloud server is being retrieved.
     * @return The first cloud server containing the player, or `null` if none is found.
     */
    fun P.getCloudServer() =
        VitalCloudNet4Driver
            .getCloudServers {
                it
                    .readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
                    .map { it.uniqueId }
                    .any { it == getPlayerUniqueId(this) }
            }.firstOrNull()

    /**
     * Retrieves the first non-proxy cloud server where the current player is present.
     *
     * This method filters through all currently running cloud servers to find the first server
     * that meets the following conditions:
     * - The server has the current player (identified by their unique identifier) in its player list.
     * - The server is not marked as a proxy service.
     *
     * If no such server is found, the method returns null.
     *
     * @receiver The player instance for whom the non-proxy cloud server is being retrieved.
     * @return The first non-proxy cloud server containing the player, or `null` if none is found.
     */
    fun P.getNonProxyCloudServer() =
        VitalCloudNet4Driver
            .getCloudServers {
                it
                    .readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
                    .map { it.uniqueId }
                    .any { it == getPlayerUniqueId(this) } &&
                    !it.isProxy()
            }.firstOrNull()

    /**
     * Determines whether the service represented by this `ServiceInfoSnapshot` is a proxy.
     *
     * This method checks the environment type of the service configuration to identify if it
     * belongs to a known proxy environment. Recognized proxy environments include:
     * - "JAVA_PROXY"
     * - "PE_PROXY"
     * - "BUNGEECORD"
     * - "VELOCITY"
     * - "WATERDOG_PE"
     *
     * @receiver The `ServiceInfoSnapshot` instance representing the service.
     * @return `true` if the service is a proxy; otherwise, `false`.
     */
    fun ServiceInfoSnapshot.isProxy() =
        when (configuration().processConfig().environment()) {
            "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true
            else -> false
        }

    /**
     * Retrieves the total number of players across all cloud servers for a specific task.
     *
     * This method fetches all cloud servers for the provided task name and calculates the
     * total count of players by summing the player counts from all retrieved servers.
     *
     * @param taskName The name of the task for which the cloud servers are queried.
     * @return The total number of players across all servers for the specified task.
     */
    fun getPlayerCount(taskName: String) =
        VitalCloudNet4Driver
            .getCloudServers(taskName)
            .map { it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf()).size }
            .ifEmpty { return 0 }
            .reduce(Integer::sum)

    /**
     * The `Spigot` object provides specific implementations for CloudNet integration with the
     * Spigot platform. It extends the `CloudNet4Bridge` class, utilizing `SpigotPlayer` as
     * the player type.
     *
     * This object focuses on managing player-related operations within the Spigot environment,
     * particularly handling player-specific UUID retrieval. As an extension of `CloudNet4Bridge`,
     * it supplies functionality for bridging the CloudNet system with Spigot servers.
     *
     * @see VitalCloudNet4Bridge
     */
    object Spigot : VitalCloudNet4Bridge<SpigotPlayer> {
        override fun getPlayerUniqueId(player: SpigotPlayer) = player.uniqueId
    }

    /**
     * The Bungee object serves as a bridge implementation for CloudNet, tailored specifically for
     * handling BungeePlayer instances. This object facilitates interaction with the CloudNet system,
     * providing methods for retrieving player-specific information and enabling seamless integration
     * between the BungeeCord proxy and the CloudNet services.
     *
     * This object overrides the `getPlayerUniqueId` method from the CloudNet4Bridge interface,
     * ensuring that the unique identifier for a player is retrieved correctly based on the
     * BungeePlayer's properties.
     *
     * @see VitalCloudNet4Bridge
     */
    object Bungee : VitalCloudNet4Bridge<BungeePlayer> {
        override fun getPlayerUniqueId(player: BungeePlayer) = player.uniqueId!!
    }
}
