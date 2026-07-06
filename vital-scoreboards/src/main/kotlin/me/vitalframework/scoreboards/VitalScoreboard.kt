package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalEntity
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import java.util.UUID

/**
 * Internal base class for all scoreboards.
 */
open class VitalScoreboard : VitalEntity<UUID> {
    override var id: UUID = UUID.randomUUID()
    val bukkitScoreboard = Bukkit.getScoreboardManager().newScoreboard

    /**
     * Updates this scoreboard with the given [title] and [lines].
     */
    fun update(
        title: String,
        lines: List<String>,
    ): Objective {
        for (objective in bukkitScoreboard.objectives) {
            objective.unregister()
        }

        val objective =
            bukkitScoreboard.getObjective(
                PlainTextComponentSerializer
                    .plainText()
                    .serialize(LegacyComponentSerializer.legacySection().deserialize(title)),
            ) ?: bukkitScoreboard.registerNewObjective(
                PlainTextComponentSerializer
                    .plainText()
                    .serialize(LegacyComponentSerializer.legacySection().deserialize(title)),
                Criteria.DUMMY,
                MiniMessage.miniMessage().deserialize(title),
            )

        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName(MiniMessage.miniMessage().deserialize(title))

        // Reset scores for existing entries
        for (entry in bukkitScoreboard.entries) {
            bukkitScoreboard.resetScores(entry)
        }

        // Update each line.
        for (lineIndex in lines.indices) {
            val score =
                objective.getScore(
                    LegacyComponentSerializer
                        .legacySection()
                        .serialize(
                            MiniMessage.miniMessage().deserialize(lines[lineIndex]),
                        ) + "\u00A7".repeat(lineIndex),
                )

            score.score = lines.size - lineIndex
        }

        return objective
    }

    /**
     * Internal function; adds a new team with the given [name] to this scoreboard  and updates it via [update].
     * If the given team is already in this scoreboard, this function does nothing.
     */
    fun addTeam(
        name: String,
        title: String,
        lines: List<String>,
        init: Team.() -> Unit,
    ) {
        if (bukkitScoreboard.getTeam(name) != null) {
            return
        }

        val team = bukkitScoreboard.registerNewTeam(name)
        init(team)
        update(title, lines)
    }

    /**
     * Internal function; removes the team with the given [name] from this scoreboard and updates it via [update].
     * If the given team is not in this scoreboard, this function does nothing.
     */
    fun removeTeam(
        name: String,
        title: String,
        lines: List<String>,
    ) {
        val team = bukkitScoreboard.getTeam(name) ?: return
        team.unregister()
        update(title, lines)
    }

    /**
     * Internal function; adds the given [player] to this scoreboard and updates it via [update].
     * If the given [player] is already in this scoreboard, this function does nothing.
     */
    fun addPlayer(
        player: SpigotPlayer,
        title: String,
        lines: List<String>,
    ) {
        if (player.scoreboard == bukkitScoreboard) {
            return
        }

        player.scoreboard = bukkitScoreboard
        update(title, lines)
    }

    /**
     * Internal function; removes the given [player] from this scoreboard and updates it via [update].
     * If the given [player] is not in this scoreboard, this function does nothing.
     */
    fun removePlayer(
        player: SpigotPlayer,
        title: String,
        lines: List<String>,
    ) {
        if (player.scoreboard != bukkitScoreboard) {
            return
        }

        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        update(title, lines)
    }
}
