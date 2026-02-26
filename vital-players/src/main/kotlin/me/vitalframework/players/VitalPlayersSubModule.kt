package me.vitalframework.players

import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/**
 * Defines the official vital-players submodule, which is displayed when Vital starts.
 * It contains the Vital players system, which can be used to get a custom player-management solution,
 * useful when you need to store player-specific data on its own isolated instance, which can later be retrieved at any time.
 */
@SubModule("vital-players")
class VitalPlayersSubModule(
    val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            logger.info("Will use class '${vitalPlayersConfigurationProperties.playerClass.name}' for new Vital managed player instances.")
        } catch (e: Exception) {
            logger.error(
                "Error while installing 'vital-players', please make sure the class for custom Vital managed player instances '${vitalPlayersConfigurationProperties.playerClassName}' exists and can be reached by Vital's classloader '${javaClass.classLoader.javaClass.name}'.",
                e,
            )
        }
    }

    @ConditionalOnMissingBean
    @Bean
    fun vitalPlayerRepository(): VitalPlayerRepository = VitalPlayerRepository()

    @ConditionalOnMissingBean
    @Bean
    fun vitalPlayerService(vitalPlayerRepository: VitalPlayerRepository): VitalPlayerService = VitalPlayerService(vitalPlayerRepository)

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Conditional(RequiresSpigot::class)
    @Configuration
    class Spigot {
        @Order(Ordered.HIGHEST_PRECEDENCE)
        @ConditionalOnMissingBean
        @Bean
        fun spigotVitalPlayerListener(
            vitalPlayerService: VitalPlayerService,
            vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
        ) = VitalPlayerListener.Spigot(vitalPlayerService, vitalPlayersConfigurationProperties)
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Conditional(RequiresBungee::class)
    @Configuration
    class Bungee {
        @Order(Ordered.HIGHEST_PRECEDENCE)
        @ConditionalOnMissingBean
        @Conditional(RequiresBungee::class)
        @Bean
        fun bungeeVitalPlayerListener(
            vitalPlayerService: VitalPlayerService,
            vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
        ) = VitalPlayerListener.Bungee(vitalPlayerService, vitalPlayersConfigurationProperties)
    }
}
