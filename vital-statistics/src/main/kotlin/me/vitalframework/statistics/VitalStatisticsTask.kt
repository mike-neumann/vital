package me.vitalframework.statistics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

interface VitalStatisticsTask {
    val statisticsService: VitalStatisticsService

    @RequiresSpigot
    @Component
    class Spigot(
        override val statisticsService: VitalStatisticsService,
    ) : VitalStatisticsTask {
        @Scheduled(fixedRate = 50)
        suspend fun handleTask() =
            withContext(Dispatchers.IO) {
                statisticsService.handleTick()
            }
    }

    @RequiresBungee
    @Component
    class Bungee(
        override val statisticsService: VitalStatisticsService,
    ) : VitalStatisticsTask {
        @Scheduled(fixedRate = 50)
        suspend fun handleTask() =
            withContext(Dispatchers.IO) {
                statisticsService.handleTick()
            }
    }
}
