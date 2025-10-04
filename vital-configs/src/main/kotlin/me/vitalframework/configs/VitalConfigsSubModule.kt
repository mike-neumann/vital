package me.vitalframework.configs

import me.vitalframework.VitalSubModule
import me.vitalframework.getRequiredAnnotation
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.constructor.Constructor
import kotlin.reflect.KClass

@Component("vital-configs")
class VitalConfigsSubModule : VitalSubModule()
typealias SnakeYamlConstructor = Constructor

/**
 * Retrieves the VitalConfig.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalConfig.Info annotation of this class.
 */
fun Class<out VitalConfig>.getInfo(): VitalConfig.Info = getRequiredAnnotation<VitalConfig.Info>()

/**
 * Retrieves the VitalConfig.Info annotation associated with this class.
 *
 * @receiver the class for which the annotation is to be retrieved.
 * @return the VitalConfig.Info annotation of this class.
 */
fun KClass<out VitalConfig>.getInfo(): VitalConfig.Info = java.getInfo()

/**
 * Retrieves the VitalConfig.Info annotation associated with this instance.
 *
 * @receiver the instance for which the annotation is to be retrieved.
 * @return the VitalConfig.Info annotation of this instance.
 */
fun VitalConfig.getInfo(): VitalConfig.Info = javaClass.getInfo()
