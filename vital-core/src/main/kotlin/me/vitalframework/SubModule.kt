package me.vitalframework

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a specific class as a Vital submodule.
 * Should be used in combination of [VitalSubModule].
 * Internally delegates functionality to [Component].
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubModule(
    @get:AliasFor(annotation = Component::class)
    val value: String
)
