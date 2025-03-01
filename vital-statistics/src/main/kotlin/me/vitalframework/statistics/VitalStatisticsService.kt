package me.vitalframework.statistics

import me.vitalframework.logger
import org.springframework.stereotype.Service

@Service
class VitalStatisticsService(val statisticsConfig: VitalStatisticsConfig) {
    private val log = logger()
    final var lastTickTime = 0L
        private set
    final var lastSecondTime = 0L
        private set
    final var ticks = 0
        private set
    final var tps = 0
        private set
    private val _lastTps = mutableMapOf<Long, Int>()
    val lastTps: Map<Long, Int> get() = _lastTps
    private val _lastUnhealthyTps = mutableMapOf<Long, Int>()
    val lastUnhealthyTps: Map<Long, Int> get() = _lastUnhealthyTps

    fun handleTick() {
        val currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - lastTickTime >= statisticsConfig.maxTaskInactiveTolerance) {
            log.warn("vital-statistics has detected increased scheduler inconsistency of ${currentTimeMillis - lastTickTime} millis")
            log.warn("This could indicate bad server-performance / health")
        }

        if (currentTimeMillis - lastSecondTime >= 1_000) {
            // one second has passed
            lastSecondTime = System.currentTimeMillis()
            tps = ticks
            ticks = 0
            _lastTps[lastSecondTime] = tps

            if (_lastTps.size > statisticsConfig.maxTpsTaskCache) {
                _lastTps.remove(_lastTps.keys.first())
            }

            if (tps < statisticsConfig.minTps) {
                _lastUnhealthyTps[System.currentTimeMillis()] = tps

                if (_lastUnhealthyTps.size > statisticsConfig.maxTpsTaskCache) {
                    _lastUnhealthyTps.remove(_lastUnhealthyTps.keys.first())
                }
            }
        }

        lastTickTime = System.currentTimeMillis()
        ticks += 1
    }
}