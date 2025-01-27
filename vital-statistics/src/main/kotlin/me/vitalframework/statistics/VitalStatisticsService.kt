package me.vitalframework.statistics

import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.Vital.log
import me.vitalframework.statistics.config.VitalStatisticsConfig
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class VitalStatisticsService(
    val statisticsConfig: VitalStatisticsConfig
) {
    var lastTickTime = 0L
    var lastSecondTime = 0L
    var ticks = 0
    var tps = 0
    val lastTps = mutableMapOf<Long, Int>()
    val lastUnhealthyTps = mutableMapOf<Long, Int>()

    fun handleTick() {
        val currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - lastTickTime >= statisticsConfig.maxTaskInactiveTolerance) {
            log().warn("vital-statistics has detected increased scheduler inconsistency of {} millis", currentTimeMillis - lastTickTime);
            log().warn("This could indicate bad server-performance / health");
        }

        if (currentTimeMillis - lastSecondTime >= 1_000) {
            // one second has passed
            lastSecondTime = System.currentTimeMillis()
            tps = ticks
            ticks = 0
            lastTps[lastSecondTime] = tps

            if (lastTps.size > statisticsConfig.maxTpsTaskCache) {
                lastTps.remove(lastTps.keys.first())
            }

            if (tps < statisticsConfig.minTps) {
                lastUnhealthyTps[System.currentTimeMillis()] = tps;

                if (lastUnhealthyTps.size > statisticsConfig.maxTpsTaskCache) {
                    lastUnhealthyTps.remove(lastUnhealthyTps.keys.first())
                }
            }
        }

        lastTickTime = System.currentTimeMillis()
        ticks += 1
    }
}