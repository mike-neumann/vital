package me.vitalframework.cloudnet4.bridge

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.registry.ServiceRegistry
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.modules.bridge.BridgeDocProperties
import eu.cloudnetservice.modules.bridge.player.PlayerManager
import me.vitalframework.BungeePlayer
import me.vitalframework.SpigotPlayer
import me.vitalframework.cloudnet4.driver.CloudNet4Driver
import java.util.*

interface CloudNet4Bridge<P> {
    companion object {
        val serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry::class.java)!!
        val playerManager = serviceRegistry.firstProvider(PlayerManager::class.java)!!
    }

    fun getPlayerUniqueId(player: P): UUID
    fun P.getPlayerExecutor() = playerManager.playerExecutor(getPlayerUniqueId(this))
    fun P.connect(serverName: String) = getPlayerExecutor().connect(serverName)
    fun P.runCommand(command: String) = getPlayerExecutor().spoofCommandExecution(command)

    fun P.getCloudServer() = CloudNet4Driver.getCloudServers {
        it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
            .map { it.uniqueId }
            .any { it == getPlayerUniqueId(this) }
    }.firstOrNull()

    fun P.getNonProxyCloudServer() = CloudNet4Driver.getCloudServers {
        it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf())
            .map { it.uniqueId }
            .any { it == getPlayerUniqueId(this) } && !it.isProxy()
    }.firstOrNull()

    fun ServiceInfoSnapshot.isProxy() = when (configuration().processConfig().environment()) {
        "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true
        else -> false
    }

    fun getPlayerCount(taskName: String) = CloudNet4Driver.getCloudServers(taskName)
        .map { it.readPropertyOrDefault(BridgeDocProperties.PLAYERS, listOf()).size }
        .reduce(Integer::sum)

    object Spigot : CloudNet4Bridge<SpigotPlayer> {
        override fun getPlayerUniqueId(player: SpigotPlayer) = player.uniqueId
    }

    object Bungee : CloudNet4Bridge<BungeePlayer> {
        override fun getPlayerUniqueId(player: BungeePlayer) = player.uniqueId!!
    }
}