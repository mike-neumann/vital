package me.vitalframework

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

interface VitalBStatsInitialization : InitializingBean {
    @RequiresSpigot
    @Configuration
    class Spigot(
        private val plugin: SpigotPlugin,
        private val vitalBStatsConfigurationProperties: VitalBStatsConfigurationProperties,
    ) : VitalBStatsInitialization {
        final override fun afterPropertiesSet() {
            if (!vitalBStatsConfigurationProperties.enabled) return

            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            SpigotBStatsMetrics(plugin, 27673)

            // then setup the custom bstats metrics
            if (vitalBStatsConfigurationProperties.pluginId == null) return
            SpigotBStatsMetrics(plugin, vitalBStatsConfigurationProperties.pluginId!!)
        }
    }

    @RequiresBungee
    @Configuration
    class Bungee(
        private val plugin: BungeePlugin,
        private val vitalBStatsConfigurationProperties: VitalBStatsConfigurationProperties,
    ) : VitalBStatsInitialization {
        final override fun afterPropertiesSet() {
            if (!vitalBStatsConfigurationProperties.enabled) return

            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            BungeeBStatsMetrics(plugin, 27674)

            // then setup the custom bstats metrics
            if (vitalBStatsConfigurationProperties.pluginId == null) return
            BungeeBStatsMetrics(plugin, vitalBStatsConfigurationProperties.pluginId!!)
        }
    }
}
