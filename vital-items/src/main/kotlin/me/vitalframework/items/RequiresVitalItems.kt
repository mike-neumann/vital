package me.vitalframework.items

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * This annotation is used to indicate that a particular class or function has a dependency
 * on the `VitalItemsSubModule` class from the `me.vitalframework.items` package.
 *
 * It acts as a conditional check and ensures that the annotated class or function
 * is only eligible for execution or registration if the `VitalItemsSubModule` class
 * is present in the classpath.
 *
 * This annotation is typically used in environments where conditional loading of
 * components based on the presence of specific libraries is required.
 *
 * It can be applied to classes or functions and is retained at runtime.
 */
@ConditionalOnClass(name = ["me.vitalframework.items.VitalItemsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalItems
