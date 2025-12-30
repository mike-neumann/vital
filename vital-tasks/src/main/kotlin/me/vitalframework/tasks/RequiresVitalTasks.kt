package me.vitalframework.tasks

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-tasks submodule is used.
 * If not running with the vital-tasks submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalTasks
 * @Component
 * public class MyVitalTasksBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.tasks.VitalTasksSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalTasks
