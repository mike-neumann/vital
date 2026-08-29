package dev.vitalframework.cloudnet4.driver

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.provider.CloudServiceFactory
import eu.cloudnetservice.driver.provider.CloudServiceProvider
import eu.cloudnetservice.driver.provider.ServiceTaskProvider
import eu.cloudnetservice.driver.service.ServiceConfiguration
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.driver.service.ServiceTask
import java.util.function.Predicate

/**
 * If you are running a CloudNET server, this will be the class to use when you want to communicate with it.
 * This class is a wrapper for the CloudNET-Driver module and provides easier-to-use APIs for it.
 */
object VitalCloudNet4Driver {
    @JvmStatic
    val cloudServiceProvider
        get() = InjectionLayer.ext().instance(CloudServiceProvider::class.java)!!

    @JvmStatic
    val serviceTaskProvider
        get() = InjectionLayer.ext().instance(ServiceTaskProvider::class.java)!!

    @JvmStatic
    val cloudServiceFactory
        get() = InjectionLayer.ext().instance(CloudServiceFactory::class.java)!!

    /**
     * Gets all servers of the given [taskName], e.g. `Lobby`, `Bedwars`.
     */
    @JvmStatic
    fun getCloudServers(taskName: String) = cloudServiceProvider.servicesByTask(taskName)

    /**
     * Gets a single server by its [serverName], e.g. `Lobby-1`, `Lobby-2`, etc.
     */
    @JvmStatic
    fun getCloudServer(serverName: String) = cloudServiceProvider.serviceByName(serverName)

    /**
     * Gets the [ServiceTask] of the given [taskName].
     */
    @JvmStatic
    fun getServerTask(taskName: String) = serviceTaskProvider.serviceTask(taskName)

    /**
     * Stops the server with the given [serverName].
     * If no server was found with the given [serverName], this function does nothing.
     */
    @JvmStatic
    fun stopCloudServer(serverName: String) = getCloudServer(serverName)?.provider()?.delete()

    /**
     * Gets all currently running servers that match the given [predicate].
     */
    @JvmStatic
    @JvmOverloads
    fun getCloudServers(predicate: Predicate<ServiceInfoSnapshot> = Predicate { true }) =
        cloudServiceProvider
            .runningServices()
            .filter(predicate::test)

    /**
     * Gets the [ServiceTask] by calling [getServerTask] with the given [taskName] and attempts to start a new server from it.
     * If no task exists for the given [taskName], this function does nothing.
     */
    @JvmStatic
    fun startCloudServer(taskName: String) =
        getServerTask(taskName)?.let { cloudServiceFactory.createCloudService(ServiceConfiguration.builder(it).build()) }
}
