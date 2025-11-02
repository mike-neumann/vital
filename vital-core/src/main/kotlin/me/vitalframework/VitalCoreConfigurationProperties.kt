package me.vitalframework

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "vital.core")
@Configuration
data class VitalCoreConfigurationProperties(
    val bstats: BStats = BStats(),
) {
    data class BStats(
        val enabled: Boolean = true,
        val pluginId: Int? = null,
    )
}
