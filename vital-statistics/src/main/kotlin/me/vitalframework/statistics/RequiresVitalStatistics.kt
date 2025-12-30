package me.vitalframework.statistics

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a class to only be loaded as a bean, when the vital-statistics submodule is used.
 * If not running with the vital-statistics submodule, the annotated bean will not be instantiated by spring.
 *
 * Must be used in combination with [Component].
 *
 * ```java
 * @RequiresVitalStatistics
 * @Component
 * public class MyVitalStatisticsBean {
 *   // ...
 * }
 * ```
 */
@ConditionalOnClass(name = ["me.vitalframework.statistics.VitalStatisticsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalStatistics
