package me.vitalframework

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

interface VitalBStatsInitialization<T> : InitializingBean {
    val plugin: T
    val vitalCoreConfigurationProperties: VitalCoreConfigurationProperties

    @RequiresSpigot
    @Configuration
    class Spigot(
        override val plugin: SpigotPlugin,
        override val vitalCoreConfigurationProperties: VitalCoreConfigurationProperties,
    ) : VitalBStatsInitialization<SpigotPlugin> {
        final override fun afterPropertiesSet() {
            if (!vitalCoreConfigurationProperties.bstats.enabled) return

            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            SpigotBStatsMetrics(plugin, 27673)

            // then setup the custom bstats metrics
            if (vitalCoreConfigurationProperties.bstats.pluginId == null) return
            SpigotBStatsMetrics(plugin, vitalCoreConfigurationProperties.bstats.pluginId!!)
        }
    }

    @RequiresBungee
    @Configuration
    class Bungee(
        override val plugin: BungeePlugin,
        override val vitalCoreConfigurationProperties: VitalCoreConfigurationProperties,
    ) : VitalBStatsInitialization<BungeePlugin> {
        final override fun afterPropertiesSet() {
            if (!vitalCoreConfigurationProperties.bstats.enabled) return

            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            BungeeBStatsMetrics(plugin, 27674)

            // then setup the custom bstats metrics
            if (vitalCoreConfigurationProperties.bstats.pluginId == null) return
            BungeeBStatsMetrics(plugin, vitalCoreConfigurationProperties.bstats.pluginId!!)
        }
    }
}
