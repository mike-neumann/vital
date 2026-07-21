package dev.vitalframework.statistics

import dev.vitalframework.RequiresSpigot
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
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
