package me.vitalframework.cloudnet4.driver

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.provider.CloudServiceFactory
import eu.cloudnetservice.driver.provider.CloudServiceProvider
import eu.cloudnetservice.driver.provider.ServiceTaskProvider
import eu.cloudnetservice.driver.service.ServiceConfiguration
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot

/**
 * The `CloudNet4Driver` object serves as the primary interface to interact with the
 * CloudNet 4 cloud infrastructure, providing access to core functionalities
 * such as cloud service management, service tasks, and cloud server operations.
 */
object VitalCloudNet4Driver {
    /**
     * Represents the cloud service provider instance used to interact with cloud infrastructure services.
     *
     * This variable is used to perform operations on cloud services, such as retrieving servers
     * by task name, server name, or filtering running services based on custom predicates.
     *
     * The instance is provided via dependency injection and is expected to be preconfigured
     * to access and manage cloud resources.
     */
    val cloudServiceProvider = InjectionLayer.ext().instance(CloudServiceProvider::class.java)!!

    /**
     * A constant that provides an instance of the ServiceTaskProvider, initialized via dependency injection.
     * It is used to get service tasks by their respective names.
     */
    val serviceTaskProvider = InjectionLayer.ext().instance(ServiceTaskProvider::class.java)!!

    /**
     * A factory instance for creating cloud services, using a dependency injection framework.
     * This property is initialized with an instance of `CloudServiceFactory` obtained
     * from the injection layer. It is used to create and configure cloud service instances
     * based on specific requirements and configurations.
     */
    val cloudServiceFactory = InjectionLayer.ext().instance(CloudServiceFactory::class.java)!!

    /**
     * Retrieves all cloud servers associated with the specified task name.
     *
     * This function queries the cloud service provider to fetch all running services
     * that are linked to the task name provided.
     *
     * @param taskName The name of the task to filter the cloud servers by.
     * @return A list of cloud servers corresponding to the specified task name.
     */
    fun getCloudServers(taskName: String) = cloudServiceProvider.servicesByTask(taskName)

    /**
     * Retrieves a cloud server instance by its name from the cloud service provider.
     *
     * @param serverName the name of the server to retrieve
     * @return the instance of the cloud server, if found
     */
    fun getCloudServer(serverName: String) = cloudServiceProvider.serviceByName(serverName)

    /**
     * Retrieves the server task associated with the given task name.
     *
     * @param taskName The name of the task to retrieve from the service task provider.
     * @return The server task corresponding to the specified task name.
     */
    fun getServerTask(taskName: String) = serviceTaskProvider.serviceTask(taskName)

    /**
     * Stops a cloud server by its name. This method retrieves the server instance
     * from the cloud service provider and deletes it to stop the server.
     *
     * @param serverName the name of the server to stop
     */
    fun stopCloudServer(serverName: String) = getCloudServer(serverName)!!.provider().delete()

    /**
     * Retrieves all currently running cloud servers that match the given predicate.
     *
     * This function filters the list of all running cloud services provided by the
     * cloud service provider using the specified predicate. By default, the predicate
     * allows all available services to be included.
     *
     * @param predicate A function defining the filtering criteria for cloud servers.
     *                  The predicate takes a `ServiceInfoSnapshot` and returns true
     *                  for services to be included in the resulting list. Defaults to
     *                  including all services.
     * @return A list of cloud servers satisfying the given predicate.
     */
    @JvmOverloads
    inline fun getCloudServers(predicate: (ServiceInfoSnapshot) -> Boolean = { true }) =
        cloudServiceProvider
            .runningServices()
            .filter(predicate)

    /**
     * Starts a cloud server based on the given task name.
     *
     * @param taskName The name of the task for which a cloud server needs to be started.
     */
    fun startCloudServer(taskName: String) =
        cloudServiceFactory.createCloudService(
            ServiceConfiguration.builder(getServerTask(taskName)!!).build(),
        )
}
