package me.vitalframework.localization

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Annotation to indicate that a class requires the presence of the `VitalLocalizationSubModule`
 * for proper functionality within the Vital framework.
 *
 * Classes annotated with `RequiresVitalLocalization` are only loaded or activated when the
 * `VitalLocalizationSubModule` is present in the classpath. This is particularly useful for
 * conditional bean activation or for ensuring that localization features are available as a
 * dependency for the annotated class.
 *
 * The `VitalLocalizationSubModule` provides support for player-specific localization capabilities,
 * including retrieving localized messages based on player-specific locales.
 *
 * Usage:
 * - Apply this annotation to any class that depends on localization functionality and ensure it is only
 *   activated or loaded if the localization module (`VitalLocalizationSubModule`) exists in the environment.
 *
 * Note:
 * - This annotation is retained at runtime.
 * - It is intended for use on classes only.
 * - Relies on checking the classpath for `me.vitalframework.localization.VitalLocalizationSubModule`.
 *
 * See Also:
 * - `VitalLocalizationSubModule`: Provides the core localization functionality for the framework.
 * - `VitalPlayer`: Core abstraction for player entities in the Vital framework, which supports locale assignment.
 * - `VitalPlayerService`: Manages `VitalPlayer` instances, which may utilize localization for personalized content.
 */
@ConditionalOnClass(name = ["me.vitalframework.localization.VitalLocalizationSubModule"])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalLocalization
