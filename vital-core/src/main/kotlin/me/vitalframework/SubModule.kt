package me.vitalframework

import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor

/**
 * Convenience-annotation to mark a specific class as a Vital submodule.
 * Should be used in combination of [VitalSubModule].
 * Internally delegates functionality to [Configuration].
 */
@Configuration
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubModule(
    @get:AliasFor(annotation = Configuration::class)
    val value: String,
)
