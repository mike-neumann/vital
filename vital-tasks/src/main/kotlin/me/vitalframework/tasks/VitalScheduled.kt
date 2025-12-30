package me.vitalframework.tasks

import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit

/**
 * Convenience-annotation to mark functions as schedulers to be managed by the internal Vital scheduler.
 * It is recommended to use this annotation for frequent jobs that should run blocking to each-other (not the main thread).
 * To schedule "normal" business-level tasks, use Spring's [Scheduled] annotation.
 *
 * ```java
 * // Run every 5 seconds.
 * @VitalScheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
 * public void broadcastMessage() {
 *   for (final var player : Bukkit.getOnlinePlayers() {
 *     player.sendMessage("Hello from Vital-scheduled task!")
 *   }
 * }
 * ```
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class VitalScheduled(
    val initialDelay: Long = 0,
    val initialDelayString: String = "",
    val fixedDelay: Long = 0,
    val fixedDelayString: String = "",
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    val timeUnitString: String = "",
)
