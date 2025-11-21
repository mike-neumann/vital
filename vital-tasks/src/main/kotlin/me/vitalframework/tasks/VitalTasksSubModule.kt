package me.vitalframework.tasks

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-tasks")
class VitalTasksSubModule(
    val vitalRepeatableTasks: List<VitalRepeatableTask<*, *, *>>,
    val vitalCountdownTasks: List<VitalCountdownTask<*, *, *>>,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        for (vitalRepeatableTask in vitalRepeatableTasks) {
            logger.info("Repeatable task '${vitalRepeatableTask::class.java.name}' successfully registered")
        }

        for (vitalCountdownTask in vitalCountdownTasks) {
            logger.info("Countdown task '${vitalCountdownTask::class.java.name}' successfully registered")
        }
    }
}
