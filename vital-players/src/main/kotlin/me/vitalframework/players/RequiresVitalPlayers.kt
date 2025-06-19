package me.vitalframework.players

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass

/**
 * Indicates that the annotated class or method requires the `VitalPlayers` submodule to be present.
 *
 * This annotation acts as a conditional check to ensure compatibility with the `VitalPlayersSubModule`.
 * It can be applied to classes or methods where functionality depends on the availability of the
 * `VitalPlayers` components in the runtime environment.
 *
 * Usage of this annotation ensures that the annotated component will only be loaded if the required
 * submodule is available, preventing runtime errors caused by missing dependencies.
 *
 * Applicable targets: Classes and functions.
 *
 * Retention policy: Runtime.
 */
@ConditionalOnClass(name = ["me.vitalframework.players.VitalPlayersSubModule"])
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresVitalPlayers
