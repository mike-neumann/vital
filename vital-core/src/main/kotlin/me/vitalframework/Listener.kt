package me.vitalframework

import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a specific class as a listener.
 * Should be used in combination of [VitalListener].
 * Internally delegates functionality to [Component].
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Listener
