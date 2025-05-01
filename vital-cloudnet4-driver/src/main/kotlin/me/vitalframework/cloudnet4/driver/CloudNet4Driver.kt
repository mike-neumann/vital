package me.vitalframework.cloudnet4.driver

import eu.cloudnetservice.driver.inject.InjectionLayer
import eu.cloudnetservice.driver.provider.*
import eu.cloudnetservice.driver.service.ServiceConfiguration
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot

object CloudNet4Driver {
    val CLOUD_SERVICE_PROVIDER = InjectionLayer.ext().instance(CloudServiceProvider::class.java)!!
    val SERVICE_TASK_PROVIDER = InjectionLayer.ext().instance(ServiceTaskProvider::class.java)!!
    val CLOUD_SERVICE_FACTORY = InjectionLayer.ext().instance(CloudServiceFactory::class.java)!!

    fun getCloudServers(taskName: String) = CLOUD_SERVICE_PROVIDER.servicesByTask(taskName)
    fun getCloudServer(serverName: String) = CLOUD_SERVICE_PROVIDER.serviceByName(serverName)
    fun getServerTask(taskName: String) = SERVICE_TASK_PROVIDER.serviceTask(taskName)
    fun stopCloudServer(serverName: String) = getCloudServer(serverName)!!.provider().delete()

    @JvmOverloads
    inline fun getCloudServers(predicate: (ServiceInfoSnapshot) -> Boolean = { true }) = CLOUD_SERVICE_PROVIDER.runningServices()
        .filter(predicate)

    fun startCloudServer(taskName: String) = CLOUD_SERVICE_FACTORY.createCloudService(
        ServiceConfiguration.builder(getServerTask(taskName)!!).build()
    )
}