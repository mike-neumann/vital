package me.vitalframework.scoreboards

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot

class VitalScoreboardContent internal constructor(title: String) {
    var title = title
        set(value) {
            field = value
            update()
        }
    val bukkitScoreboard = Bukkit.getScoreboardManager().newScoreboard
    private val _teams = mutableListOf<VitalScoreboardTeam>()
    val teams: List<VitalScoreboardTeam> get() = _teams

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

    fun addTeam(team: VitalScoreboardTeam) {
        if (team in _teams) return
        _teams.add(team)
        update()
    }

    fun removeTeam(team: VitalScoreboardTeam) {
        if (team !in _teams) return
        _teams.remove(team)
        update()
    }
}