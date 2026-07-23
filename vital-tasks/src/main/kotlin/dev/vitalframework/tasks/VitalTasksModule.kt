package dev.vitalframework.tasks

import dev.vitalframework.BungeePlugin
import dev.vitalframework.RequiresBungee
import dev.vitalframework.RequiresSpigot
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.Vital
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@VitalModule.Info(value = "vital-tasks")
class VitalTasksModule(
    val vitalRepeatableTasks: List<VitalRepeatableTask<*, *, *>>,
    val vitalCountdownTasks: List<VitalCountdownTask<*, *, *>>,
) : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        for (vitalRepeatableTask in vitalRepeatableTasks) {
            logger.info("Repeatable task '${vitalRepeatableTask::class.java.name}' successfully registered")
        }

        for (vitalCountdownTask in vitalCountdownTasks) {
            logger.info("Countdown task '${vitalCountdownTask::class.java.name}' successfully registered")
        }
    }

    override fun onDisable() {
        logger.debug("Shutting down Vital scheduler.")
        val vitalScheduler = Vital.context.getBean<VitalScheduler>()
        vitalScheduler.shutdown()
        logger.debug("Vital schedular shut down.")
    }

    @Conditional(RequiresSpigot::class)
    @Configuration
    class Spigot {
        @ConditionalOnMissingBean
        @Bean
        fun vitalSchedulerSpigot(
            environment: Environment,
            plugin: SpigotPlugin,
        ) = VitalScheduler.Spigot(environment, plugin)
    }

    @Conditional(RequiresBungee::class)
    @Configuration
    class Bungee {
        @ConditionalOnMissingBean
        @Bean
        fun vitalSchedulerBungee(
            environment: Environment,
            plugin: BungeePlugin,
        ): VitalScheduler = VitalScheduler.Bungee(environment, plugin)
    }
}
