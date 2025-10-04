package me.vitalframework.tasks

import me.vitalframework.VitalSubModule
import me.vitalframework.getRequiredAnnotation
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component("vital-tasks")
class VitalTasksSubModule : VitalSubModule()

/**
 * Retrieves the VitalRepeatableTask.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalRepeatableTask.Info annotation of this class.
 */
fun Class<out VitalRepeatableTask<*, *, *>>.getInfo(): VitalRepeatableTask.Info = getRequiredAnnotation<VitalRepeatableTask.Info>()

/**
 * Retrieves the VitalRepeatableTask.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalRepeatableTask.Info annotation of this class.
 */
fun KClass<out VitalRepeatableTask<*, *, *>>.getInfo(): VitalRepeatableTask.Info = java.getInfo()

/**
 * Retrieves the VitalRepeatableTask.Info annotation associated with this instance.
 *
 * @receiver the instance for which the annotation is to be retrieved.
 * @return the VitalRepeatableTask.Info annotation of this instance.
 */
fun VitalRepeatableTask<*, *, *>.getInfo(): VitalRepeatableTask.Info = javaClass.getInfo()

/**
 * Retrieves the VitalCountdownTask.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalCountdownTask.Info annotation of this class.
 */
fun Class<out VitalCountdownTask<*, *, *>>.getInfo(): VitalCountdownTask.Info = getRequiredAnnotation<VitalCountdownTask.Info>()

/**
 * Retrieves the VitalCountdownTask.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalCountdownTask.Info annotation of this class.
 */
fun KClass<out VitalCountdownTask<*, *, *>>.getInfo(): VitalCountdownTask.Info = java.getInfo()

/**
 * Retrieves the VitalCountdownTask.Info annotation associated with this instance.
 *
 * @receiver the instance for which the annotation is to be retrieved.
 * @return the VitalCountdownTask.Info annotation of this instance.
 */
fun VitalCountdownTask<*, *, *>.getInfo(): VitalCountdownTask.Info = javaClass.getInfo()
