package me.vitalframework.cloudnet4.bridge

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.registry.ServiceRegistry
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.modules.bridge.BridgeDocProperties
import eu.cloudnetservice.modules.bridge.player.PlayerManager
import me.vitalframework.cloudnet4.driver.CloudNet4Driver
import java.util.*

object CloudNet4Bridge {
    val serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry::class.java)!!
    val playerManager = serviceRegistry.firstProvider(PlayerManager::class.java)!!

    fun getPlayerExecutor(playerUniqueId: UUID) = playerManager.playerExecutor(playerUniqueId)

    fun isProxy(serviceInfoSnapshot: ServiceInfoSnapshot) =
        when (serviceInfoSnapshot.configuration().processConfig().environment()) {
            "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true
            else -> false
        }

    fun getCloudServer(playerUniqueId: UUID) = CloudNet4Driver.getCloudServers {
        it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
            .map { it.uniqueId }
            .any { it == playerUniqueId }
    }.firstOrNull()

    fun getNonProxyCloudServer(playerUniqueId: UUID) = CloudNet4Driver.getCloudServers {
        it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
            .map { it.uniqueId }
            .any { it == playerUniqueId } && !isProxy(it)
    }.firstOrNull()

    fun connect(playerUniqueId: UUID, serverName: String) {
        getPlayerExecutor(playerUniqueId).connect(serverName)
    }

    fun getPlayerCount(taskName: String) = CloudNet4Driver.getCloudServers(taskName)
        .map { it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf()).size }
        .reduce { a, b -> Integer.sum(a, b) }

    fun runCommand(playerUniqueId: UUID, command: String) {
        getPlayerExecutor(playerUniqueId).spoofCommandExecution(command)
    }
}