package me.vitalframework.minigames

import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@VitalModule.Info(value = "vital-minigames")
class VitalMinigamesModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-minigames' has been enabled, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-minigames' in the correct server environment, e.g. Spigot, Paper.")
        }
    }

    @Conditional(RequiresSpigot::class)
    @Configuration
    class Spigot {
        private val logger = logger()

        @ConditionalOnMissingBean
        @Bean
        fun vitalGlobalMinigameService(
            plugin: SpigotPlugin,
            vitalGlobalMinigameStates: List<VitalGlobalMinigameState>,
        ): VitalGlobalMinigameService {
            if (vitalGlobalMinigameStates.isEmpty()) {
                logger.warn(
                    "No global minigame states were found by 'vital-minigames'. The global mini game service will be rendered useless.",
                )
            }

            return VitalGlobalMinigameService(plugin, vitalGlobalMinigameStates)
        }

        @ConditionalOnMissingBean
        @Bean
        fun vitalMinigameInstanceRepository(): VitalMinigameInstanceRepository = VitalMinigameInstanceRepository()

        @ConditionalOnMissingBean
        @Bean
        fun vitalMinigameInstanceService(
            vitalMinigameInstanceRepository: VitalMinigameInstanceRepository,
            plugin: SpigotPlugin,
        ): VitalMinigameInstanceService = VitalMinigameInstanceService(vitalMinigameInstanceRepository, plugin)
    }
}
