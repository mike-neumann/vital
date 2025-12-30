package me.vitalframework.cloudnet4.bridge

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-cloudnet4-bridge submodule is used.
 * If not running with the vital-cloudnet4-bridge submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalCloudnet4Bridge
 * @Component
 * public class MyVitalCloudnet4BridgeBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.cloudnet4.bridge.VitalCloudNet4BridgeSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalCloudNet4Bridge
