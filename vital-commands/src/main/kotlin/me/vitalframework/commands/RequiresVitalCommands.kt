package me.vitalframework.commands

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-commands submodule is used.
 * If not running with the vital-commands submodule, the annotated bean will not be instatiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalCommands
 * @Component
 * public class MyVitalCommandsBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.commands.VitalCommandsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalCommands
