package me.vitalframework.inventories

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-inventories submodules is used.
 * If not running with the vital-inventories submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalInventories
 * @Component
 * public class MyVitalInventoriesBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.inventories.VitalInventoriesSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalInventories
