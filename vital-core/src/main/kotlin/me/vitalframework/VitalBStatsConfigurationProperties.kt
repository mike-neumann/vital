package me.vitalframework

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "vital.core.bstats")
@Configuration
data class VitalBStatsConfigurationProperties(
    val enabled: Boolean = true,
    val pluginId: Int? = null,
)
