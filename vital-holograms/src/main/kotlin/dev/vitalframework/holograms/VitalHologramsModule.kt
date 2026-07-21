package dev.vitalframework.holograms

import dev.vitalframework.RequiresSpigot
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

/**
 * Defines the official vital-holograms module, which is displayed when Vital starts.
 * It contains the Vital holograms system, which can be used to create global and per-player based holograms.
 */
@VitalModule.Info(value = "vital-holograms")
class VitalHologramsModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-holograms' has been enabled, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-holograms' in the correct server environment, e.g. Spigot, Paper.")
        }
    }

    @Conditional(RequiresSpigot::class)
    @Configuration(proxyBeanMethods = false)
    class Spigot {
        @ConditionalOnMissingBean
        @Bean
        fun vitalHologramRepository() = VitalHologramRepository()

        @ConditionalOnMissingBean
        @Bean
        fun vitalHologramService(
            plugin: SpigotPlugin,
            vitalHologramRepository: VitalHologramRepository,
        ) = VitalHologramService(plugin, vitalHologramRepository)

        @ConditionalOnMissingBean
        @Bean
        fun vitalPerPlayerHologramListener(vitalHologramService: VitalHologramService) =
            VitalPerPlayerHologramListener(vitalHologramService)
    }
}
