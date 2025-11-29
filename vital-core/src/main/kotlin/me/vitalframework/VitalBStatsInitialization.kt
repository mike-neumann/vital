package me.vitalframework

import me.vitalframework.VitalCoreSubModule.Companion.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

interface VitalBStatsInitialization<T> : InitializingBean {
    val plugin: T
    val vitalCoreConfigurationProperties: VitalCoreConfigurationProperties

    override fun afterPropertiesSet() {
        val logger = logger()

        if (!vitalCoreConfigurationProperties.bstats.enabled) {
            logger.info("Opting out of bStats, Vital will not send any metrics to bStats for this plugin")
            return
        }

        logger.info("Thanks for using bStats metrics for this plugin!")
        logger.info("bStats collects some basic information about your plugin (use-count, server-count, player-count, etc.)")
        logger.info("More details are available at https://bstats.org/getting-started")
        logger.info("To opt out of bStats, set 'vital.core.bstats.enabled' to 'false' in your application configuration")

        // so bstats stops whining about their goofy package relocation step
        System.setProperty("bstats.relocatecheck", "false")

        startVitalBStats()

        if (vitalCoreConfigurationProperties.bstats.pluginId == null) {
            logger.info("bStats enabled, but no plugin id provided; Vital will only send its own analytics to bStats...")
            logger.info("To enable bStats metrics for your plugin, set 'vital.core.bstats.pluginId' in your application configuration")
            logger.info("Please follow the bStats documentation for more information")
            return
        }

        startCustomBStats(vitalCoreConfigurationProperties.bstats.pluginId!!)
    }

    fun startVitalBStats()

    fun startCustomBStats(pluginId: Int)

    @RequiresSpigot
    @Configuration
    class Spigot(
        override val plugin: SpigotPlugin,
        override val vitalCoreConfigurationProperties: VitalCoreConfigurationProperties,
    ) : VitalBStatsInitialization<SpigotPlugin> {
        companion object {
            const val PLUGIN_ID = 27673
        }

        override fun startVitalBStats() {
            SpigotBStatsMetrics(plugin, PLUGIN_ID)
        }

        override fun startCustomBStats(pluginId: Int) {
            SpigotBStatsMetrics(plugin, pluginId)
        }
    }

    @RequiresBungee
    @Configuration
    class Bungee(
        override val plugin: BungeePlugin,
        override val vitalCoreConfigurationProperties: VitalCoreConfigurationProperties,
    ) : VitalBStatsInitialization<BungeePlugin> {
        companion object {
            const val PLUGIN_ID = 27674
        }

        override fun startVitalBStats() {
            BungeeBStatsMetrics(plugin, PLUGIN_ID)
        }

        override fun startCustomBStats(pluginId: Int) {
            BungeeBStatsMetrics(plugin, pluginId)
        }
    }
}
