package me.vitalframework.configs

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * This annotation is used to conditionally enable specific configurations or components
 * within an application when the `VitalConfigsSubModule` class is present on the classpath.
 *
 * It serves as a mechanism for integrating with the Vital Framework by ensuring
 * that the required submodule dependency is available before applying the annotated component.
 *
 * Apply this annotation to classes or functions to denote their dependency
 * on the presence of the `VitalConfigsSubModule`.
 */
@ConditionalOnClass(name = ["me.vitalframework.configs.VitalConfigsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalConfigs
