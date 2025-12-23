package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
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
    val title: (SpigotPlayer) -> String,
    vararg var lines: (SpigotPlayer) -> String,
) : VitalScoreboard {
    private val _scoreboardContent = mutableMapOf<UUID, VitalScoreboardContent>()

    /**
     * Provides a mapping between player UUIDs and their corresponding scoreboard content.
     *
     * This property serves as a read-only view of the internal `_scoreboardContent` map,
     * which stores instances of [VitalScoreboardContent] associated with individual players.
     * It allows querying the scoreboard content for a specific player by their unique identifier.
     *
     * The mapping ensures that each player is assigned custom scoreboard content, enabling dynamic
     * updates and personalized scoreboard interactions within the system.
     *
     * Accessing this property does not allow for modifications; updates to the scoreboard content
     * must be performed through designated methods within the enclosing class.
     */
    val scoreboardContent: Map<UUID, VitalScoreboardContent>
        get() = _scoreboardContent

    /**
     * Updates the scoreboard content for the specified player.
     * If the player is registered in the scoreboard content, their active scoreboard is reset.
     * The method ensures that the player's scoreboard is updated with the latest content
     * and then set back to their specific assigned scoreboard.
     *
     * @param player The player whose scoreboard content needs to be updated.
     */
    fun update(player: SpigotPlayer) {
        if (player.uniqueId !in _scoreboardContent) return
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        updateContent(player)
        player.scoreboard = _scoreboardContent[player.uniqueId]!!.bukkitScoreboard
    }

    /**
     * Updates the content and visual representation of the scoreboard for the specified player.
     * Handles the score updates, text formatting, and adjusting the display order of lines.
     *
     * @param player The player whose scoreboard content should be updated.
     */
    private fun updateContent(player: SpigotPlayer) {
        if (player.uniqueId !in _scoreboardContent) return
        val scoreboard = _scoreboardContent[player.uniqueId]!!

        scoreboard.update()
        val objective =
            scoreboard.bukkitScoreboard.getObjective(
                PlainTextComponentSerializer
                    .plainText()
                    .serialize(LegacyComponentSerializer.legacySection().deserialize(scoreboard.title())),
            )
        val lines = applyLines(player)

        for (lineIndex in lines.indices) {
            val score =
                objective!!.getScore(
                    LegacyComponentSerializer
                        .legacySection()
                        .serialize(
                            MiniMessage.miniMessage().deserialize(lines[lineIndex]),
                        ) + "\u00A7".repeat(lineIndex),
                )

            score.score = lines.size - lineIndex
        }
    }

    /**
     * Adds a player to the scoreboard system associated with this instance.
     * If the player is already present, the method exits without applying any changes.
     *
     * @param player The player to be added to the scoreboard system.
     */
    fun addPlayer(player: SpigotPlayer) {
        if (player.uniqueId in _scoreboardContent) return
        _scoreboardContent[player.uniqueId] = VitalScoreboardContent { title(player) }
        update(player)
    }

    /**
     * Removes the specified player from the scoreboard content and resets their scoreboard
     * to the default main scoreboard of the server.
     *
     * @param player The player to be removed from the scoreboard.
     */
    fun removePlayer(player: SpigotPlayer) {
        if (player.uniqueId !in _scoreboardContent) return
        _scoreboardContent.remove(player.uniqueId)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }

    /**
     * Applies each line of the scoreboard content to the specified player.
     * This method transforms the existing lines for the specific player context.
     *
     * @param player The player for whom the scoreboard lines are being applied.
     */
    private fun applyLines(player: SpigotPlayer) = lines.map { it(player) }
}
