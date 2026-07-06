package me.vitalframework.tasks

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
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

    @ConditionalOnMissingBean
    @Bean
    fun vitalScheduler(environment: Environment): VitalScheduler = VitalScheduler(environment)
}
