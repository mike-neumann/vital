package dev.vitalframework.scoreboards

import java.util.function.Supplier

/**
 * Defines a global scoreboard implementation within the Vital-Framework.
 * Global scoreboards should be used when displaying data, that is not tied to a specific player.
 *
 * ```java
 * @Bean
 * public VitalGlobalScoreboard myGlobalScoreboard() {
 *   return new VitalGlobalScoreboard(
 *     () -> "MyGlobalScoreboard",
 *     () -> "Line 1",
 *     () -> "Line 2",
 *     () -> "Line 3"
 *   );
 * }
 * ```
 */
class VitalGlobalScoreboard(
    title: Supplier<String>,
    vararg lines: Supplier<String>,
) : VitalScoreboard() {
    var title = title
        set(value) {
            field = value
            update(value::get) { lines.map { it.get() } }
        }

    var lines = lines
        set(value) {
            field = value
            update(title::get) { value.map { it.get() } }
        }
}
