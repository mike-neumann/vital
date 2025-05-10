package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import java.util.function.Supplier

/**
 * Represents a global scoreboard in the Vital framework.
 *
 * The `VitalGlobalScoreboard` manages the state and content of a scoreboard shared across all its associated players.
 * It allows for dynamically updating the scoreboard content and managing the players that view the scoreboard.
 *
 * This class leverages the following key elements:
 * - A centralized scoreboard content represented by `VitalScoreboardContent`.
 * - Reactive updates to manage synchronization of displayed data.
 * - Functions for handling players, including adding, removing, and updating their individual scoreboards.
 *
 * @constructor Creates a `VitalGlobalScoreboard` instance with a specified title and dynamic lines.
 * @param title The title of the scoreboard.
 * @param lines A vararg of `Supplier<String>` instances that dynamically provide the lines of the scoreboard.
 */
class VitalGlobalScoreboard(title: String, vararg var lines: Supplier<String>) : VitalScoreboard {
    /**
     * Represents the content of the scoreboard used in the context of the `VitalGlobalScoreboard`.
     *
     * This variable provides a centralized way to manage and update the structure and data
     * displayed on the scoreboard. It operates as an instance of `VitalScoreboardContent`,
     * initialized with the title of the scoreboard.
     *
     * Key responsibilities of this content include:
     * - Managing the visual representation and data structure of the scoreboard.
     * - Handling updates to the scoreboard when changes are made to its title or teams.
     * - Providing support for interactivity with player and team information.
     */
    val scoreboardContent = VitalScoreboardContent(title)
    private val _players = mutableListOf<SpigotPlayer>()

    /**
     * Retrieves the list of players currently associated with the scoreboard.
     *
     * This list provides a read-only view of the players who are actively being tracked
     * or displayed on the scoreboard. Modifications to this list are managed internally
     * by the corresponding methods for adding or removing players.
     *
     * @return A list of `SpigotPlayer` instances tracked by the scoreboard.
     */
    val players: List<SpigotPlayer> get() = _players

    /**
     * Updates the specified player's scoreboard.
     *
     * This method assigns the Bukkit main scoreboard to the player and then
     * sets it to the custom scoreboard associated with the `scoreboardContent`.
     *
     * @param player The player whose scoreboard should be updated.
     */
    private fun update(player: SpigotPlayer) {
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        player.scoreboard = scoreboardContent.bukkitScoreboard
    }

    /**
     * Updates the state of the global scoreboard and all associated players.
     *
     * This method performs the following:
     * - Updates the core content of the scoreboard by invoking `updateContent`.
     * - Iterates over all players associated with the scoreboard and updates
     *   their individual scoreboard configuration using the `update` method.
     *
     * The updates ensure that both the scoreboard content and the settings for
     * each player are synchronized with the current state.
     */
    fun update() {
        updateContent()

        for (player in _players) {
            update(player)
        }
    }

    /**
     * Updates the content of the scoreboard by re-evaluating its lines and setting their respective scores.
     *
     * This method performs the following operations:
     * - Calls the update method on the associated `scoreboardContent` to ensure the scoreboard structure is updated.
     * - Retrieves the objective associated with the scoreboard title, using text serialization/deserialization methods.
     * - Iterates through the provided list of line suppliers (`lines`), generates a line of text from each supplier,
     *   and assigns a unique score to each line for display ordering.
     *
     * The scores assigned decrease from the total number of lines to ensure proper order of appearance in the scoreboard.
     * Each line is distinguished by appending unique separator characters based on its position.
     *
     * This method assumes the existence of a valid `bukkitScoreboard` and ensures that the display content
     * of the scoreboard reflects the current state of the specified `lines`.
     */
    fun updateContent() {
        scoreboardContent.update()
        val objective = scoreboardContent.bukkitScoreboard.getObjective(
            PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacySection().deserialize(scoreboardContent.title))
        )

        for (lineIndex in lines.indices) {
            val lineSupplier = lines[lineIndex]
            val line = lineSupplier.get()
            val score = objective!!.getScore(
                LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(line)) + "\u00A7".repeat(lineIndex)
            )

            score.score = lines.size - lineIndex
        }
    }

    /**
     * Adds a player to the scoreboard. If the player is already present, no action is taken.
     * Updates the scoreboard after adding the player.
     *
     * @param player The player to be added to the scoreboard.
     */
    fun addPlayer(player: SpigotPlayer) {
        if (player in _players) return
        _players.add(player)
        update()
    }

    /**
     * Removes the specified player from the scoreboard. If the player is not associated with
     * the scoreboard, the method has no effect. Once removed, the player's scoreboard is reset
     * to the server's main scoreboard. After removal, the scoreboard is updated to reflect the changes.
     *
     * @param player The player to be removed from the scoreboard.
     */
    fun removePlayer(player: SpigotPlayer) {
        if (player !in _players) return
        _players.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        update()
    }
}