package me.vitalframework

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

interface VitalBStatsInitialization : InitializingBean {
    @RequiresSpigot
    @Configuration
    class Spigot(
        private val plugin: SpigotPlugin,
        private val configurationProperties: VitalBStatsConfigurationProperties,
    ) : VitalBStatsInitialization {
        final override fun afterPropertiesSet() {
            if (!configurationProperties.enabled) return

            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            SpigotBStatsMetrics(plugin, 27673)

            // then setup the custom bstats metrics
            if (configurationProperties.pluginId == null) return
            SpigotBStatsMetrics(plugin, configurationProperties.pluginId!!)
        }
    }

    @RequiresBungee
    @Configuration
    class Bungee(
        private val plugin: BungeePlugin,
        private val configurationProperties: VitalBStatsConfigurationProperties,
    ) : VitalBStatsInitialization {
        final override fun afterPropertiesSet() {
            if (!configurationProperties.enabled) return

            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            BungeeBStatsMetrics(plugin, 27674)

            // then setup the custom bstats metrics
            if (configurationProperties.pluginId == null) return
            BungeeBStatsMetrics(plugin, configurationProperties.pluginId!!)
        }
    }
}
