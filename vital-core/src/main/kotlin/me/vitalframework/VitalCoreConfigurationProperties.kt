package me.vitalframework

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "vital.core")
@Component
class VitalCoreConfigurationProperties(
    val bstats: BStats = BStats(),
) {
    data class BStats(
        val enabled: Boolean = true,
        val pluginId: Int? = null,
    )
}
