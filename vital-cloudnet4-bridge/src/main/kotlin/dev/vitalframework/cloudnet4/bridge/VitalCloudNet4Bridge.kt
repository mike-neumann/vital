package dev.vitalframework.cloudnet4.bridge

import dev.vitalframework.BungeePlayer
import dev.vitalframework.SpigotPlayer
import dev.vitalframework.cloudnet4.driver.VitalCloudNet4Driver
import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.registry.ServiceRegistry
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.modules.bridge.BridgeDocProperties
import eu.cloudnetservice.modules.bridge.player.PlayerManager
import eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor
import java.util.UUID

/**
 * If you are running a CloudNET server, this will be the class to use when you want to communicate with it.
 * This class is a wrapper for the CloudNET-Bridge module and provides easier-to-use APIs for it.
 */
interface VitalCloudNet4Bridge<P> {
    companion object {
        @JvmStatic
        val serviceRegistry get() = InjectionLayer.ext().instance(ServiceRegistry::class.java)!!

        @JvmStatic
        val playerManager get() = serviceRegistry.defaultInstance(PlayerManager::class.java)!!
    }

    /**
     * Internal function to get the [UUID] of the given [player].
     */
    fun getPlayerUniqueId(player: P): UUID

    /**
     * Gets the [PlayerExecutor] instance for the given player.
     */
    fun P.getPlayerExecutor() = playerManager.playerExecutor(getPlayerUniqueId(this))

    /**
     * Gets the [PlayerExecutor] for the given player and tries to connect the player to the given [serverName].
     * Note that the [serverName] is the fully qualified service name, e.g. `Lobby-1`, `Lobby-2`, etc.
     */
    fun P.connect(serverName: String) = getPlayerExecutor().connect(serverName)

    /**
     * Gets the [PlayerExecutor] for the given player and executes the given [command] as the player.
     */
    fun P.runCommand(command: String) = getPlayerExecutor().spoofCommandExecution(command)

    /**
     * Scans all currently active servers for the first server which the given player is currently connected to.
     * If the player is not online, this function returns null.
     *
     * Note that this function can also return proxy servers like BungeeCord, Velocity, etc.
     * To get the current game server of the player, use [getNonProxyCloudServer].
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
     * Scans all currently active game servers (non-proxy) for the first server which the given player is currently connected to.
     * If the player is not online, this function returns null.
     *
     * Note that this function will not return any proxy server like BungeeCord, Velocity, etc.
     * To also include those servers in the search, use [getCloudServer].
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
     * Checks it the given [ServiceInfoSnapshot] is a known Minecraft proxy server, like BungeeCord, Velocity, etc.
     */
    fun ServiceInfoSnapshot.isProxy() =
        when (configuration().processConfig().environment()) {
            "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true
            else -> false
        }

    /**
     * Gets the total player count for all services of the given [taskName].
     * This function should only be used to get a total sum of ALL SERVERS OF THE GIVEN [taskName].
     *
     * If you want to get the player count of a single server, use [getPlayerCount].
     *
     * Note that this function will never return null, even if the given [taskName] does not resolve to any actual servers.
     */
    fun getTotalPlayerCount(taskName: String) =
        VitalCloudNet4Driver
            .getCloudServers(taskName)
            .map { it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf()).size }
            .ifEmpty { return 0 }
            .reduce(Integer::sum)

    object Spigot : VitalCloudNet4Bridge<SpigotPlayer> {
        override fun getPlayerUniqueId(player: SpigotPlayer) = player.uniqueId
    }

    object Bungee : VitalCloudNet4Bridge<BungeePlayer> {
        override fun getPlayerUniqueId(player: BungeePlayer) = player.uniqueId!!
    }
}
