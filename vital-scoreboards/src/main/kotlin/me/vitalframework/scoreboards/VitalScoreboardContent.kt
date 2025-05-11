package me.vitalframework.scoreboards

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

/**
 * Represents the content of a scoreboard within the Vital framework, providing functionality
 * for managing objectives, display settings, teams, and more. This class encapsulates the logic
 * for dynamically updating and rendering scoreboard elements in a Minecraft server environment.
 *
 * @constructor Creates an instance of VitalScoreboardContent with an initial title.
 * The title determines the display name of the scoreboard's objective.
 */
class VitalScoreboardContent internal constructor(title: String) {
    private val _teams = mutableListOf<VitalScoreboardTeam>()

    /**
     * Represents the title of the scoreboard. This property is used to define the name and display text
     * for the scoreboard's objective. When changed, it triggers an update to ensure the scoreboard
     * reflects the new title visually and functionally.
     *
     * Modifying this property will automatically update the scoreboard's state, including its objective,
     * display slot, and any associated teams.
     */
    var title = title
        set(value) {
            field = value
            update()
        }

    /**
     * Represents the underlying Bukkit scoreboard instance used in the VitalScoreboardContent class.
     * This scoreboard is used to manage and display teams, players, and other scoreboard elements
     * within a Minecraft server environment.
     *
     * Provides a platform for creating and updating teams, assigning players, and rendering
     * custom scoreboard content dynamically.
     */
    val bukkitScoreboard = Bukkit.getScoreboardManager().newScoreboard

    /**
     * Provides a read-only list of all the teams currently managed within the scoreboard content.
     * The returned list reflects the current state of teams and can be used for querying or
     * iterating over all the teams added to this scoreboard.
     *
     * Modifications to the underlying internal team list will automatically be reflected in this property.
     *
     * @return An unmodifiable view of the current teams in the scoreboard content.
     */
    val teams: List<VitalScoreboardTeam> get() = _teams

    /**
     * Updates the state of the scoreboard, including its objective, display, and associated teams.
     *
     * This method ensures that the scoreboard's title and display slot are correctly set and the
     * entries are reset. It also iterates through all associated teams, invoking their respective
     * update methods to synchronize their state with the scoreboard.
     *
     * Key operations performed by this method:
     * - Retrieves or creates an objective for the scoreboard, based on the current title.
     * - Updates the objective's display slot and display name.
     * - Resets scores for all existing entries in the scoreboard.
     * - Calls the `update` method for each team associated with the scoreboard.
     */
    fun update() {
        val objective = bukkitScoreboard.getObjective(
            PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(title))
        ) ?: bukkitScoreboard.registerNewObjective(
            PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(title)),
            Criteria.DUMMY,
            MiniMessage.miniMessage().deserialize(title)
        )

        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName(MiniMessage.miniMessage().deserialize(title))
        // Reset scores for existing entries
        for (entry in bukkitScoreboard.entries) {
            bukkitScoreboard.resetScores(entry)
        }
        // Update each scoreboard team
        for (team in _teams) {
            team.update()
        }
    }

    /**
     * Adds a new team to the scoreboard, provided it is not already present.
     * Once added, triggers an update to reflect the changes on the scoreboard.
     *
     * @param team The team to be added to the scoreboard.
     */
    fun addTeam(team: VitalScoreboardTeam) {
        if (team in _teams) return
        _teams.add(team)
        update()
    }

    /**
     * Removes the specified team from the scoreboard content. If the team is not already present,
     * the method does nothing. After removing the team, the scoreboard is updated to reflect the changes.
     *
     * @param team The instance of [VitalScoreboardTeam] to be removed from the scoreboard content.
     */
    fun removeTeam(team: VitalScoreboardTeam) {
        if (team !in _teams) return
        _teams.remove(team)
        update()
    }
}