package me.vitalframework.inventories

import me.vitalframework.VitalSubModule
import me.vitalframework.getRequiredAnnotation
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component("vital-inventories")
class VitalInventoriesSubModule : VitalSubModule()

/**
 * Retrieves the VitalInventory.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalInventory.Info annotation of this class.
 */
fun Class<out VitalInventory>.getInfo(): VitalInventory.Info = getRequiredAnnotation<VitalInventory.Info>()

/**
 * Retrieves the VitalInventory.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalInventory.Info annotation of this class.
 */
fun KClass<out VitalInventory>.getInfo(): VitalInventory.Info = java.getInfo()

/**
 * Retrieves the VitalInventory.Info annotation associated with this instance.
 *
 * @receiver the instance for which the annotation is to be retrieved.
 * @return the VitalInventory.Info annotation of this instance.
 */
fun VitalInventory.getInfo(): VitalInventory.Info = javaClass.getInfo()

/**
 * Retrieves the VitalPagedInventory.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalPagedInventory.Info annotation of this class.
 */
fun Class<out VitalPagedInventory>.getInfo(): VitalPagedInventory.Info = getRequiredAnnotation<VitalPagedInventory.Info>()

/**
 * Retrieves the VitalPagedInventory.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalPagedInventory.Info annotation of this class.
 */
fun KClass<out VitalPagedInventory>.getInfo(): VitalPagedInventory.Info = java.getInfo()

/**
 * Retrieves the VitalPagedInventory.Info annotation associated with this instance.
 *
 * @receiver the instance for which the annotation is to be retrieved.
 * @return the VitalPagedInventory.Info annotation of this instance.
 */
fun VitalPagedInventory.getVitalPagedInventoryInfo(): VitalPagedInventory.Info = javaClass.getInfo()
