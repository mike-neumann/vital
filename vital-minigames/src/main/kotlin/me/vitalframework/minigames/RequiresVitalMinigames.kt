package me.vitalframework.minigames

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class or function requires the `VitalMinigamesSubModule` to be available.
 *
 * This annotation is used as a marker to conditionally enable certain classes or functions
 * in environments where the `VitalMinigamesSubModule` is present. The presence of the
 * `VitalMinigamesSubModule` is determined through its class name at runtime.
 *
 * It is commonly used in systems that depend on the API of the `VitalMinigames` module
 * to ensure compatibility and prevent errors when the module is not available in the
 * runtime environment.
 *
 * The annotation is reflective of dependencies on minigame-related services or states,
 * specifically leveraging the functionality provided by the `VitalMinigames` submodule.
 *
 * Applying this annotation protects the annotated class or function from being loaded
 * or executed when the `VitalMinigamesSubModule` is not detected, ensuring safety in
 * modular environments.
 *
 * Targeted towards classes and functions, the annotation is retained at runtime,
 * allowing the dependency check to occur dynamically.
 */
@ConditionalOnClass(name = ["me.vitalframework.minigames.VitalMinigamesSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalMinigames
