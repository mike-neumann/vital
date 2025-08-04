package me.vitalframework.statistics

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class VitalStatisticsConfig {
    @Value($$"${plugin.min-tps:16}")
    var minTps = 0

    @Value($$"${plugin.max-task-inactive-tolerance:250}")
    var maxTaskInactiveTolerance = 0

    @Value($$"${plugin.max-tps-task-cache:16}")
    var maxTpsTaskCache = 0
}