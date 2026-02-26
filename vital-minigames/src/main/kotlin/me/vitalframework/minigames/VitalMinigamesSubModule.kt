package me.vitalframework.minigames

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
 * Defines the official vital-minigames submodule, which is displayed when Vital starts.
 * It contains the Vital minigames system, which ban be used to create global (server-wide) games, or world-instanced games.
 */
@SubModule("vital-minigames")
class VitalMinigamesSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-minigames' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
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
