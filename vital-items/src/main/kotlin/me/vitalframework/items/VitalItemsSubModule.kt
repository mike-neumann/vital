package me.vitalframework.items

import me.vitalframework.VitalSubModule
import me.vitalframework.getRequiredAnnotation
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component("vital-items")
class VitalItemsSubModule : VitalSubModule()

/**
 * Retrieves the VitalItem.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalItem.Info annotation of this class.
 */
fun Class<out VitalItem>.getInfo(): VitalItem.Info = getRequiredAnnotation<VitalItem.Info>()

/**
 * Retrieves the VitalItem.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalItem.Info annotation of this class.
 */
fun KClass<out VitalItem>.getInfo(): VitalItem.Info = java.getInfo()

/**
 * Retrieves the VitalItem.Info annotation associated with this instance.
 *
 * @receiver the instance for which the annotation is to be retrieved.
 * @return the VitalItem.Info annotation of this instance.
 */
fun VitalItem.getInfo(): VitalItem.Info = javaClass.getInfo()
