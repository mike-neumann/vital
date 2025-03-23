package me.vitalframework.cloudnet4.driver

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.provider.*
import eu.cloudnetservice.driver.service.ServiceConfiguration
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot

object CloudNet4Driver {
    val cloudServiceProvider = InjectionLayer.ext().instance(CloudServiceProvider::class.java)!!
    val serviceTaskProvider = InjectionLayer.ext().instance(ServiceTaskProvider::class.java)!!
    val cloudServiceFactory = InjectionLayer.ext().instance(CloudServiceFactory::class.java)!!

    fun getCloudServers(taskName: String) = cloudServiceProvider.servicesByTask(taskName)
    fun getCloudServer(serverName: String) = cloudServiceProvider.serviceByName(serverName)
    fun getServerTask(taskName: String) = serviceTaskProvider.serviceTask(taskName)
    fun stopCloudServer(serverName: String) = getCloudServer(serverName)!!.provider().delete()

    @JvmOverloads
    inline fun getCloudServers(predicate: (ServiceInfoSnapshot) -> Boolean = { true }) = cloudServiceProvider.runningServices()
        .filter(predicate)

    fun startCloudServer(taskName: String) = cloudServiceFactory.createCloudService(
        ServiceConfiguration.builder(getServerTask(taskName)!!).build()
    )
}