package me.vitalframework.holograms

import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotPlugin
import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

/**
 * Defines the official vital-holograms submodule, which is displayed when Vital starts.
 * It contains the Vital holograms system, which can be used to create global and per-player based holograms.
 */
@SubModule("vital-holograms")
class VitalHologramsSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-holograms' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-holograms' in the correct server environment, e.g. Spigot, Paper.")
        }
    }

    @Conditional(RequiresSpigot::class)
    @Configuration
    class Spigot {
        private val logger = logger()

        @ConditionalOnMissingBean
        @Bean
        fun vitalHologramService(
            plugin: SpigotPlugin,
            vitalPerPlayerHologramProviders: List<VitalHologramProvider<VitalPerPlayerHologram>>,
        ): VitalHologramService {
            if (vitalPerPlayerHologramProviders.isEmpty()) {
                logger.warn(
                    "No per player hologram providers were defined. Vital will fail to hide per player holograms from other players.",
                )
            }

            return VitalHologramService(plugin, vitalPerPlayerHologramProviders)
        }

        @ConditionalOnMissingBean
        @Bean
        fun vitalPerPlayerHologramListener(vitalHologramService: VitalHologramService): VitalPerPlayerHologramListener =
            VitalPerPlayerHologramListener(vitalHologramService)
    }
}
