package me.vitalframework.minigames

import org.springframework.stereotype.Component

/**
 * Convenience-annotation to mark a specific class as a minigame state.
 * Should be used in combination with [VitalMinigameState].
 * Internally delegates functionality to [Component].
 */
@Component
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MinigameState
