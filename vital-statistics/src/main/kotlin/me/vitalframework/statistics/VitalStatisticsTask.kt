package me.vitalframework.statistics

import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.tasks.VitalScheduled
import org.springframework.stereotype.Component

interface VitalStatisticsTask {
    val statisticsService: VitalStatisticsService

    @RequiresSpigot
    @Component
    class Spigot(
        override val statisticsService: VitalStatisticsService,
    ) : VitalStatisticsTask {
        @VitalScheduled(fixedDelay = 50)
        fun handleTask() {
            statisticsService.handleTick()
        }
    }

    @RequiresBungee
    @Component
    class Bungee(
        override val statisticsService: VitalStatisticsService,
    ) : VitalStatisticsTask {
        @VitalScheduled(fixedDelay = 50)
        fun handleTask() {
            statisticsService.handleTick()
        }
    }
}
