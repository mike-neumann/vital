package me.vitalframework.players

import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@VitalModule.Info(value = "vital-players")
class VitalPlayersModule(
    val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
) : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            logger.info("Will use class '${vitalPlayersConfigurationProperties.playerClass.name}' for new Vital managed player instances.")
        } catch (e: Exception) {
            throw VitalPlayerException.CheckPlayerClass(vitalPlayersConfigurationProperties.playerClassName, e)
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
    @Configuration(proxyBeanMethods = false)
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
    @Configuration(proxyBeanMethods = false)
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
