package me.vitalframework.statistics

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "vital.statistics")
@Configuration
data class VitalStatisticsConfigurationProperties(
    val minTps: Int = 16,
    val maxTaskInactiveTolerance: Int = 250,
    val maxTpsTaskCache: Int = 16,
)
