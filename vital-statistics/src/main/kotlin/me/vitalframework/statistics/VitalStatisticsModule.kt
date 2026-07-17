package me.vitalframework.statistics

import me.vitalframework.RequiresSpigot
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@VitalModule.Info(value = "vital-statistics")
class VitalStatisticsModule : VitalModule() {
    private val logger = logger()

    override fun onEnable() {
        logger.info(
            "'vital-statistics' has been enabled and will monitor server performance. Check '/vital-stats' for server-info and '/vital-stats tps' for tps-info.",
        )
    }

    @Conditional(RequiresSpigot::class)
    @Configuration(proxyBeanMethods = false)
    class Spigot {
        @ConditionalOnMissingBean
        @Bean
        fun vitalStatisticsService(vitalStatisticsConfigurationProperties: VitalStatisticsConfigurationProperties): VitalStatisticsService =
            VitalStatisticsService(vitalStatisticsConfigurationProperties)
    }
}
