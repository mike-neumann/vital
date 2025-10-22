package me.vitalframework

import me.vitalframework.VitalCoreSubModule.Companion.getVitalInfo
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration

object VitalCustomBStatsConfig {
    @RequiresSpigot
    @Configuration
    class Spigot(
        private val plugin: SpigotPlugin,
    ) : InitializingBean {
        final override fun afterPropertiesSet() {
            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            SpigotBStatsMetrics(plugin, 27673)

            // then setup the custom bstats metrics
            val vitalInfo = Vital.metadata.mainClass.getVitalInfo()
            val bstatsPluginId = vitalInfo.bstatsPluginId
            if (bstatsPluginId == Int.MIN_VALUE) return
            SpigotBStatsMetrics(plugin, bstatsPluginId)
        }
    }

    @RequiresBungee
    @Configuration
    class Bungee(
        private val plugin: BungeePlugin,
    ) : InitializingBean {
        final override fun afterPropertiesSet() {
            // so bstats stops whining about their goofy package relocation step
            System.setProperty("bstats.relocatecheck", "false")

            // Setup Vital's bstats metrics first
            BungeeBStatsMetrics(plugin, 27674)

            // then setup the custom bstats metrics
            val vitalInfo = Vital.metadata.mainClass.getVitalInfo()
            val bstatsPluginId = vitalInfo.bstatsPluginId
            if (bstatsPluginId == Int.MIN_VALUE) return
            BungeeBStatsMetrics(plugin, bstatsPluginId)
        }
    }
}
