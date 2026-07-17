package me.vitalframework.minigames

import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.bukkit.Bukkit
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration

@VitalModule.Info(value = "vital-minigames")
class VitalMinigamesModule(
    private val vitalMinigameInstanceRepository: VitalMinigameInstanceRepository,
    private val vitalMinigameInstanceService: VitalMinigameInstanceService,
) : VitalModule() {
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

        // Warn server administrators of possible leftover minigame instance worlds.
        val leftoverWorldFiles =
            Bukkit
                .getWorldContainer()
                .walkTopDown()
                .maxDepth(1)
                .filter { it.isDirectory && it.listFiles()?.any { it.name.endsWith(VitalMinigameInstance.MARKER) } ?: false }
                .toList()
        if (leftoverWorldFiles.isNotEmpty()) {
            logger.error(
                "'vital-minigames' has detected '${leftoverWorldFiles.size}' possible leftover minigame instance worlds. This is most likely caused by an unexpected server / plugin shutdown.",
            )
            logger.error(
                "THESE INSTANCE WORLDS ARE NOT LOADED BY VITAL. IF YOU ARE NOT PLANNING TO USE THEM IN ANY OTHER WAY, DELETE THEM.",
            )
            for (file in leftoverWorldFiles) {
                logger.error("> ${file.absolutePath}")
            }
        }
    }

    override fun onDisable() {
        val instanceCount = vitalMinigameInstanceRepository.entities.size
        if (instanceCount == 0) {
            return
        }

        logger.warn(
            "'vital-minigames' is being disabled while there are still '$instanceCount' running minigame instances. Will terminate all.",
        )
        vitalMinigameInstanceService.unregisterAllInstances(
            {
                logger.warn("Terminating minigame instance with id '${it.id}'.")
            },
            {
                logger.warn("Successfully terminated minigame instance with id '${it.id}'.")
            },
        )
    }

    @Conditional(RequiresSpigot::class)
    @Configuration(proxyBeanMethods = false)
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
