package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import java.util.function.Function

/**
 * Represents a vital scoreboard customized per player. Each player has a unique set
 * of lines displayed on their scoreboard that is dynamically generated based on the provided
 * line functions.
 *
 * @property title The title of the scoreboard displayed at the top.
 * @property lines A variable number of functions that generate the content for each scoreboard
 * line based on the player's context.
 */
class VitalPerPlayerScoreboard(val title: String, vararg var lines: Function<SpigotPlayer, String>) : VitalScoreboard {
    private val _scoreboardContent = mutableMapOf<SpigotPlayer, VitalScoreboardContent>()

    /**
     * Retrieves a map containing the scoreboard content associated with each individual player.
     * The keys in the map represent instances of `SpigotPlayer`, while the values are instances
     * of `VitalScoreboardContent` that define the structure and visual representation of the
     * scoreboard for the corresponding player.
     *
     * This property is designed to manage player-specific scoreboard data within the context
     * of the `VitalPerPlayerScoreboard` system, ensuring that each player has an independent
     * scoreboard setup.
     */
    val scoreboardContent: Map<SpigotPlayer, VitalScoreboardContent> get() = _scoreboardContent

    /**
     * Updates the scoreboard content for the specified player.
     * If the player is registered in the scoreboard content, their active scoreboard is reset.
     * The method ensures that the player's scoreboard is updated with the latest content
     * and then set back to their specific assigned scoreboard.
     *
     * @param player The player whose scoreboard content needs to be updated.
     */
    fun update(player: SpigotPlayer) {
        if (!_scoreboardContent.containsKey(player)) return
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        updateContent(player)
        player.scoreboard = _scoreboardContent[player]!!.bukkitScoreboard
    }

    /**
     * Updates the content and visual representation of the scoreboard for the specified player.
     * Handles the score updates, text formatting, and adjusting the display order of lines.
     *
     * @param player The player whose scoreboard content should be updated.
     */
    private fun updateContent(player: SpigotPlayer) {
        if (!_scoreboardContent.containsKey(player)) return
        val scoreboard = _scoreboardContent[player]!!

        scoreboard.update()
        val objective = scoreboard.bukkitScoreboard.getObjective(
            PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(scoreboard.title))
        )
        val lines = applyLines(player)

        for (lineIndex in lines.indices) {
            val score = objective!!.getScore(
                LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(lines[lineIndex])) + "\u00A7".repeat(lineIndex)
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
        if (_scoreboardContent.containsKey(player)) return
        _scoreboardContent[player] = VitalScoreboardContent(title)
        update(player)
    }

    /**
     * Removes the specified player from the scoreboard content and resets their scoreboard
     * to the default main scoreboard of the server.
     *
     * @param player The player to be removed from the scoreboard.
     */
    fun removePlayer(player: SpigotPlayer) {
        if (!_scoreboardContent.containsKey(player)) return
        _scoreboardContent.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }

    /**
     * Applies each line of the scoreboard content to the specified player.
     * This method transforms the existing lines for the specific player context.
     *
     * @param player The player for whom the scoreboard lines are being applied.
     */
    private fun applyLines(player: SpigotPlayer) = lines.map { it.apply(player) }
}