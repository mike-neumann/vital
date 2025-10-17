package me.vitalframework

import org.springframework.stereotype.Component

/**
 * Annotation used to mark listener classes within the framework. Delegates functionality to the `@Component` annotation.
 * This annotation serves as a pure convenience mechanism, providing clear and consistent naming for listener classes.
 *
 * Target classes are annotated as listeners within the framework ecosystem, particularly for event-driven or dependency injection use cases.
 *
 * Usage of this annotation enables automatic Spring component scanning and injection, aligning with the framework’s conventions.
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Listener
