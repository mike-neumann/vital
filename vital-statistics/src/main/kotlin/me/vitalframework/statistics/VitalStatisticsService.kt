package me.vitalframework.statistics

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.tasks.VitalScheduled

/**
 * Internal service; handles health statistics.
 */
open class VitalStatisticsService(
    val vitalStatisticsConfigurationProperties: VitalStatisticsConfigurationProperties,
) {
    private val logger = logger()

    private val _lastTps = mutableMapOf<Long, Int>()
    val lastTps: Map<Long, Int>
        get() = _lastTps

    private val _lastUnhealthyTps = mutableMapOf<Long, Int>()
    val lastUnhealthyTps: Map<Long, Int>
        get() = _lastUnhealthyTps

    var lastTickTime = 0L
        private set
    var lastSecondTime = 0L
        private set
    var ticks = 0
        private set
    var tps = 0
        private set

    @VitalScheduled(fixedDelay = 50)
    fun handleTick() {
        val currentTimeMillis = System.currentTimeMillis()

        if (currentTimeMillis - lastTickTime >= vitalStatisticsConfigurationProperties.maxTaskInactiveTolerance) {
            logger.warn("vital-statistics has detected increased scheduler inconsistency of ${currentTimeMillis - lastTickTime} millis")
            logger.warn("This could indicate bad server-performance / health")
        }

        if (currentTimeMillis - lastSecondTime >= 1_000) {
            // one second has passed
            lastSecondTime = System.currentTimeMillis()
            tps = ticks
            ticks = 0
            _lastTps[lastSecondTime] = tps

            if (_lastTps.size > vitalStatisticsConfigurationProperties.maxTpsTaskCache) _lastTps.remove(_lastTps.keys.first())

            if (tps < vitalStatisticsConfigurationProperties.minTps) {
                _lastUnhealthyTps[System.currentTimeMillis()] = tps

                if (_lastUnhealthyTps.size > vitalStatisticsConfigurationProperties.maxTpsTaskCache) {
                    _lastUnhealthyTps.remove(
                        _lastUnhealthyTps.keys.first(),
                    )
                }
            }
        }

        lastTickTime = System.currentTimeMillis()
        ticks += 1
    }
}
