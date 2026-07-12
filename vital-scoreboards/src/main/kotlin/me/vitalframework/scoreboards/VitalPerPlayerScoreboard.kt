package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import org.bukkit.Bukkit
import java.util.UUID

/**
 * Defines a per-player based scoreboard implementation within the Vital-Framework.
 * Per-played scoreboards should be used when displaying data, that is tied to a specific player on the server.
 *
 * ```java
 * @Bean
 * public VitalPerPlayerScoreboard myPerPlayerScoreboard() {
 *   return new VitalPerPlayerScoreboard(
 *     player -> "MyPerPlayerScoreboard",
 *     player -> "Line 1 for " + player.getName(),
 *     player -> "Line 2 for " + player.getName(),
 *     player -> "Line 3 for " + player.getName()
 *   );
 * }
 * ```
 */
class VitalPerPlayerScoreboard(
    title: (SpigotPlayer) -> String,
    vararg lines: (SpigotPlayer) -> String,
) {
    /**
     * The [VitalScoreboard] instance for each player added to this scoreboard.
     */
    private val _scoreboards = mutableMapOf<UUID, VitalScoreboard>()

    val scoreboards: Map<UUID, VitalScoreboard>
        get() = _scoreboards

    /**
     * The title function for this scoreboard.
     * If modified, calls the [update] function to automatically update this scoreboard for all players.
     */
    var title = title
        set(value) {
            field = value
            update()
        }

    /**
     * The lines-functions for this scoreboard.
     * If modified, calls the [update] function to automatically update this scoreboard for all players.
     */
    var lines = lines
        set(value) {
            field = value
            update()
        }

    /**
     * Adds the given [player] to this scoreboard.
     * If the given [player] is already in this scoreboard, this function will override that scoreboard.
     */
    @JvmOverloads
    fun addPlayer(
        player: SpigotPlayer,
        update: Boolean = true,
    ) {
        _scoreboards[player.uniqueId] =
            VitalScoreboard().apply {
                this.addPlayer(
                    player,
                    if (update) ({ title(player) }) else null,
                    if (update) ({ lines.map { it(player) } }) else null,
                )
            }
    }

    /**
     * Removes the given [player] from this scoreboard.
     * If the given [player] is not in this scoreboard, this function does nothing.
     */
    @JvmOverloads
    fun removePlayer(
        player: SpigotPlayer,
        update: Boolean = true,
    ) {
        val scoreboard = _scoreboards[player.uniqueId] ?: return
        scoreboard.removePlayer(
            player,
            if (update) ({ title(player) }) else null,
            if (update) ({ lines.map { it(player) } }) else null,
        )
    }

    /**
     * Updates this scoreboard for all players.
     * If this scoreboard has no players, this function does nothing.
     */
    fun update() {
        for ((playerUniqueId, _) in scoreboards) {
            val player = Bukkit.getPlayer(playerUniqueId) ?: continue
            update(player)
        }
    }

    /**
     * Updates this scoreboard for the given [player].
     * If the given [player] is not in this scoreboard, this function does nothing.
     */
    fun update(player: SpigotPlayer) {
        val scoreboard = _scoreboards[player.uniqueId] ?: return
        scoreboard.update({ title(player) }) { lines.map { it(player) } }
    }
}
