package me.vitalframework.holograms

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Specifies that the annotated class or function requires the presence of the `VitalHologramsSubModule`.
 *
 * This annotation is used as a condition to ensure that certain functionalities or components
 * are only available if the `VitalHologramsSubModule` is present in the application context. This
 * modular approach allows for optional integration of the `VitalHolograms` subsystem, ensuring
 * that dependent features or classes are instantiated only when their required dependencies are available.
 *
 * Typically used in configurations or service classes that interact with holograms defined by the `VitalHolograms` module.
 *
 * The condition is triggered by the presence of the `VitalHologramsSubModule` class.
 *
 * Note: Classes or functions using this annotation should assume that the `VitalHolograms` subsystem is loaded
 * and functional when the condition passes.
 */
@ConditionalOnClass(name = ["me.vitalframework.holograms.VitalHologramsSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalHolograms
