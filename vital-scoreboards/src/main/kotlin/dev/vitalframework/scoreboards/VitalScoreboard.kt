package dev.vitalframework.scoreboards

import dev.vitalframework.SpigotPlayer
import dev.vitalframework.VitalEntity
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import java.util.UUID
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Internal base class for all scoreboards.
 */
open class VitalScoreboard : VitalEntity<UUID> {
    override var id: UUID = UUID.randomUUID()
    val bukkitScoreboard = Bukkit.getScoreboardManager().newScoreboard

    /**
     * Updates this scoreboard with the given [_title] and [_lines].
     */
    fun update(
        _title: Supplier<String>,
        _lines: Supplier<List<String>>,
    ): Objective {
        try {
            val title = _title.get()
            val lines = _lines.get()

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
        } catch (e: Exception) {
            throw VitalScoreboardException.Update(e)
        }
    }

    /**
     * Internal function; adds a new team with the given [name] to this scoreboard.
     * This function will only update this scoreboard if both [title] and [lines] are set.
     * If the given team is already in this scoreboard, this function does nothing.
     */
    @JvmOverloads
    fun addTeam(
        name: String,
        title: Supplier<String>? = null,
        lines: Supplier<List<String>>? = null,
        init: Consumer<Team>,
    ) {
        if (bukkitScoreboard.getTeam(name) != null) {
            return
        }

        val team = bukkitScoreboard.registerNewTeam(name)
        init.accept(team)

        if (title != null && lines != null) {
            update(title, lines)
        }
    }

    /**
     * Internal function; removes the team with the given [name] from this scoreboard.
     * This function will only update this scoreboard if both [title] and [lines] are set.
     * If the given team is not in this scoreboard, this function does nothing.
     */
    @JvmOverloads
    fun removeTeam(
        name: String,
        title: Supplier<String>? = null,
        lines: Supplier<List<String>>? = null,
    ) {
        val team = bukkitScoreboard.getTeam(name) ?: return
        team.unregister()

        if (title != null && lines != null) {
            update(title, lines)
        }
    }

    /**
     * Internal function; adds the given [player] to this scoreboard and updates it via [update].
     * This function will only update this scoreboard if both [title] and [lines] are set.
     * If the given [player] is already in this scoreboard, this function does nothing.
     */
    @JvmOverloads
    fun addPlayer(
        player: SpigotPlayer,
        title: Supplier<String>? = null,
        lines: Supplier<List<String>>? = null,
    ) {
        if (player.scoreboard == bukkitScoreboard) {
            return
        }

        player.scoreboard = bukkitScoreboard
        if (title != null && lines != null) {
            update(title, lines)
        }
    }

    /**
     * Internal function; removes the given [player] from this scoreboard and updates it via [update].
     * This function will only update this scoreboard if both [title] and [lines] are set.
     * If the given [player] is not in this scoreboard, this function does nothing.
     */
    @JvmOverloads
    fun removePlayer(
        player: SpigotPlayer,
        title: Supplier<String>? = null,
        lines: Supplier<List<String>>? = null,
    ) {
        if (player.scoreboard != bukkitScoreboard) {
            return
        }

        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        if (title != null && lines != null) {
            update(title, lines)
        }
    }
}
