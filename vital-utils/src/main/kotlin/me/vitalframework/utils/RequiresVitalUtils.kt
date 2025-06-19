package me.vitalframework.utils

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class or function requires the presence of the `VitalUtilsSubModule` class
 * from the `me.vitalframework.utils` package on the classpath.
 *
 * This annotation can be used to conditionally activate certain components or logic
 * based on the availability of the specified class.
 *
 * It is primarily used in environments where optional dependencies are supported,
 * allowing components to adapt their behavior dynamically based on the presence
 * or absence of certain classes.
 *
 * This annotation is retained at runtime and can be targeted at classes or functions.
 */
@ConditionalOnClass(name = ["me.vitalframework.utils.VitalUtilsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalUtils
