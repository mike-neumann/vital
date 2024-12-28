package me.vitalframework.statistics.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
open class VitalStatisticsConfig {
    /**
     * The minimum tps for vital to detect a "HEALTHY" server
     * Default: 16
     */
    @Value("\${plugin.min-tps:16}")
    var minTps = 0

    /**
     * The maximum allowed task inactive time measured in millis for vital to detect any timer inconsistencies
     * Default: 250
     */
    @Value("\${plugin.max-task-inactive-tolerance:250}")
    var maxTaskInactiveTolerance = 0

    /**
     * The amount of tps task reports to cache before deleting older ones
     * Default: 16
     */
    @Value("\${plugin.max-tps-task-cache:16}")
    var maxTpsTaskCache = 0
}