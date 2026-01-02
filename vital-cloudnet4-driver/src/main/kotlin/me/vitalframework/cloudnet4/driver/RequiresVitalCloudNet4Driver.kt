package me.vitalframework.cloudnet4.driver

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-cloudnet4-driver submodule is used.
 * If not running with the vital-cloudnet4-driver submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalCloudnet4Driver
 * @Component
 * public class MyVitalCloudnet4DriverBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.cloudnet4.driver.VitalCloudNet4DriverSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalCloudNet4Driver
