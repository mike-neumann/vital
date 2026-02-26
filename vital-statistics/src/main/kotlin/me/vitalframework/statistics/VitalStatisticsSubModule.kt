package me.vitalframework.statistics

import me.vitalframework.RequiresSpigot
import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

/**
 * Defines the official vital-statistics submodule, which is displayed when Vital starts.
 * It contains the Vital statistics system, which registers health checkers to your server that warn you when performance drops.
 */
@SubModule("vital-statistics")
class VitalStatisticsSubModule : VitalSubModule() {
    private val logger = logger()

    override fun onInstall() {
        logger.info(
            "'vital-statistics' has been installed and will monitor server performance. Check '/vital-stats' for server-info and '/vital-stats tps' for tps-info.",
        )
    }

    @Conditional(RequiresSpigot::class)
    @Configuration
    class Spigot {
        @ConditionalOnMissingBean
        @Bean
        fun vitalStatisticsService(vitalStatisticsConfigurationProperties: VitalStatisticsConfigurationProperties): VitalStatisticsService =
            VitalStatisticsService(vitalStatisticsConfigurationProperties)
    }
}
