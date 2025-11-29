package me.vitalframework.statistics

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class or method depends on the presence of the `VitalStatisticsSubModule` class.
 *
 * This annotation is used to conditionally load classes or methods when the `VitalStatisticsSubModule` component
 * is present in the application context, enabling integration with Vital Framework's statistics submodule.
 *
 * Can be applied at the class or function level and retains the annotation metadata at runtime.
 */
@ConditionalOnClass(name = ["me.vitalframework.statistics.VitalStatisticsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalStatistics
