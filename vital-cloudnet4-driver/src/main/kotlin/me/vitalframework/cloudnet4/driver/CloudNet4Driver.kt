package me.vitalframework.cloudnet4.driver

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.provider.CloudServiceFactory
import eu.cloudnetservice.driver.provider.CloudServiceProvider
import eu.cloudnetservice.driver.provider.ServiceTaskProvider
import eu.cloudnetservice.driver.service.ServiceConfiguration
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot
import eu.cloudnetservice.driver.service.ServiceTask

/**
 * Utility class to interact with the cloudnet v4 driver module more easily.
 *
 * @author xRa1ny
 */
object CloudNet4Driver {
    val cloudServiceProvider = InjectionLayer.ext().instance(CloudServiceProvider::class.java)!!
    val serviceTaskProvider = InjectionLayer.ext().instance(ServiceTaskProvider::class.java)!!
    val cloudServiceFactory = InjectionLayer.ext().instance(CloudServiceFactory::class.java)!!

    @JvmOverloads
    inline fun getCloudServers(predicate: (ServiceInfoSnapshot) -> Boolean = { true }): List<ServiceInfoSnapshot> =
        cloudServiceProvider.runningServices()
            .filter(predicate)

    /**
     * Gets all cloudnet services by the given task name.
     */
    fun getCloudServers(taskName: String): List<ServiceInfoSnapshot> =
        cloudServiceProvider.servicesByTask(taskName).toList()

    /**
     * Gets the cloud server by the given name; or null;
     */
    fun getCloudServer(serverName: String): ServiceInfoSnapshot? = cloudServiceProvider.serviceByName(serverName)

    /**
     * Gets the task of the given server task name.
     */
    fun getServerTask(taskName: String): ServiceTask? = serviceTaskProvider.serviceTask(taskName)

    /**
     * Attempts to start a server with the given name, e.g: Lobby-1.
     * This method will attempt to grab the task of the given server name and use its configuration.
     */
    fun startCloudServer(taskName: String) {
        cloudServiceFactory.createCloudService(
            ServiceConfiguration.builder(getServerTask(taskName)!!)
                .build()
        )
    }

    /**
     * Attempts to stop a cloud server with the given name, e.g: Lobby-1.
     */
    fun stopCloudServer(serverName: String) {
        getCloudServer(serverName)!!.provider().delete()
    }
}