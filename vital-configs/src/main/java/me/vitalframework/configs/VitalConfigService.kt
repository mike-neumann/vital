package me.vitalframework.configs

import me.vitalframework.Vital.context
import org.springframework.stereotype.Service

@Service
class VitalConfigService {
    fun getConfigs(vitalConfigClass: Class<VitalConfig>) =
        try {
            context.getBeansOfType(vitalConfigClass).values
        } catch (e: Exception) {
            mutableListOf()
        }

    fun getConfigs() = getConfigs(VitalConfig::class.java)

    fun <T : VitalConfig> getConfig(vitalConfigClass: Class<T>) =
        try {
            context.getBean<T?>(vitalConfigClass)
        } catch (e: Exception) {
            null
        }
}