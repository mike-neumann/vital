package me.vitalframework.scoreboards

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class or function requires the VitalScoreboardsSubModule
 * to be present on the classpath in order to function correctly.
 *
 * This annotation is used as a conditional mechanism for enabling or configuring
 * specific features that depend on the presence of VitalScoreboards within the
 * application's environment. If the required module is not available, the annotated
 * class or function will not be loaded or executed.
 *
 * It is particularly useful in modular or dynamically-configured applications where
 * specific components should only activate when the necessary dependencies are available.
 */
@ConditionalOnClass(name = ["me.vitalframework.scoreboards.VitalScoreboardsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalScoreboards
