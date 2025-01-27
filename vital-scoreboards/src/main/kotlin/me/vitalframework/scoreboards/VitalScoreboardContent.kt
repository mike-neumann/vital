package me.vitalframework.scoreboards

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

/**
 * Represents the content of a scoreboard in the Vital plugin framework.
 * This class manages the scoreboard's title, teams, and updates.
 */
class VitalScoreboardContent internal constructor(
    title: String,
) {
    var title = title
        set(value) {
            field = value
            update()
        }
    val bukkitScoreboard = Bukkit.getScoreboardManager()!!.newScoreboard
    val teams = mutableListOf<VitalScoreboardTeam>()

    /**
     * Updates the scoreboard content, including its title and teams.
     */
    fun update() {
        var objective = bukkitScoreboard.getObjective(
            PlainTextComponentSerializer.plainText()
                .serialize(
                    LegacyComponentSerializer.legacySection()
                        .deserialize(title)
                )
        )

        if (objective == null) {
            objective = bukkitScoreboard.registerNewObjective(
                PlainTextComponentSerializer.plainText()
                    .serialize(
                        LegacyComponentSerializer.legacySection()
                            .deserialize(title)
                    ),
                Criteria.DUMMY,
                LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title))
            )
        }

        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName =
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title))

        // Reset scores for existing entries
        bukkitScoreboard.entries.forEach {
            bukkitScoreboard.resetScores(it)
        }

        // Update each scoreboard team
        teams.forEach {
            it.update()
        }
    }

    fun addTeam(team: VitalScoreboardTeam) {
        if (team in teams) {
            return
        }

        teams.add(team)
        update()
    }

    fun removeTeam(team: VitalScoreboardTeam) {
        if (team !in teams) {
            return
        }

        teams.remove(team)
        update()
    }
}