package me.vitalframework.cloudnet4.bridge

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.registry.ServiceRegistry
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.modules.bridge.BridgeDocProperties
import eu.cloudnetservice.modules.bridge.player.PlayerManager
import me.vitalframework.cloudnet4.driver.CloudNet4Driver
import java.util.*

/**
 * Utility class for easier interaction with the cloudnet v4 bridge module.
 */
object CloudNet4Bridge {
    val serviceRegistry: ServiceRegistry = InjectionLayer.ext().instance(ServiceRegistry::class.java)
    val playerManager: PlayerManager = serviceRegistry.firstProvider(PlayerManager::class.java)

    /**
     * Gets the player executor of the given player uuid.
     */
    fun getPlayerExecutor(playerUniqueId: UUID) = playerManager.playerExecutor(playerUniqueId)

    /**
     * Checks if the given cloudnet service is a proxy.
     */
    fun isProxy(serviceInfoSnapshot: ServiceInfoSnapshot) =
        when (serviceInfoSnapshot.configuration().processConfig().environment()) {
            "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true
            else -> false
        }

    /**
     * Gets the cloudnet service the given player uuid is currently connected to.
     */
    fun getCloudServer(playerUniqueId: UUID) = CloudNet4Driver.getCloudServers {
        it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
            .map { it.uniqueId }
            .any { it == playerUniqueId }
    }.firstOrNull()

    /**
     * Gets the non-proxy cloudnet service the given player uuid is currently connected to.
     */
    fun getNonProxyCloudServer(playerUniqueId: UUID) = CloudNet4Driver.getCloudServers {
        it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
            .map { it.uniqueId }
            .any { it == playerUniqueId } && !isProxy(it)
    }.firstOrNull()

    /**
     * Connects the given player uuid with the given server name.
     */
    fun connect(playerUniqueId: UUID, serverName: String) {
        getPlayerExecutor(playerUniqueId).connect(serverName)
    }

    /**
     * Gets the player count of all cloudnet services by the given task name.
     */
    fun getPlayerCount(taskName: String) = CloudNet4Driver.getCloudServers(taskName)
        .map { it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf()).size }
        .reduce { a, b -> Integer.sum(a, b) }

    /**
     * Runs the command as the given player
     */
    fun runCommand(playerUniqueId: UUID, command: String) {
        getPlayerExecutor(playerUniqueId).spoofCommandExecution(command)
    }
}