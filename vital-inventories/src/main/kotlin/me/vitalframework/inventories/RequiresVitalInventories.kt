package me.vitalframework.inventories

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that a class or function has a dependency on the `VitalInventoriesSubModule`.
 *
 * This annotation is used to conditionally include or enable configuration elements
 * based on the presence of the `VitalInventoriesSubModule` class in the classpath.
 *
 * The annotated element will only be processed if the `VitalInventoriesSubModule` class
 * is available at runtime. This is particularly useful for setups where certain features
 * should only be enabled when specific dependencies are present.
 *
 * This annotation can be applied to both classes and functions.
 */
@ConditionalOnClass(name = ["me.vitalframework.inventories.VitalInventoriesSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalInventories
