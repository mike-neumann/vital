package dev.vitalframework.statistics

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "vital.statistics")
@Component
class VitalStatisticsConfigurationProperties(
    val minTps: Int = 16,
    val maxTaskInactiveTolerance: Int = 250,
    val maxTpsTaskCache: Int = 16,
)
