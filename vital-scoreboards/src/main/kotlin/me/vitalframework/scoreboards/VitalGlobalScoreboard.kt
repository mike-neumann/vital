package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import java.util.function.Supplier

/**
 * Manages a global scoreboard displayed to multiple players.
 * This class allows the creation and management of scoreboards that can be displayed to multiple players simultaneously.
 */
class VitalGlobalScoreboard(
    title: String,
    vararg var lines: Supplier<String>,
) : VitalScoreboard {
    val vitalScoreboardContent = VitalScoreboardContent(title)
    val players = mutableListOf<SpigotPlayer>()

    /**
     * Updates the scoreboard for a specific player.
     *
     * @param player The player for whom to update the scoreboard.
     */
    private fun update(player: SpigotPlayer) {
        player.scoreboard = Bukkit.getScoreboardManager()!!.mainScoreboard
        player.scoreboard = vitalScoreboardContent.bukkitScoreboard
    }

    /**
     * Updates the scoreboards for all users associated with this global scoreboard.
     */
    fun update() {
        updateContent()

        players.forEach {
            update(it)
        }
    }

    /**
     * Updates the content of this scoreboard, including titles and scores.
     */
    fun updateContent() {
        vitalScoreboardContent.update()

        val objective = vitalScoreboardContent.bukkitScoreboard.getObjective(
            PlainTextComponentSerializer.plainText()
                .serialize(
                    LegacyComponentSerializer.legacySection()
                        .deserialize(vitalScoreboardContent.title)
                )
        )

        lines.indices.forEach {
            val lineSupplier = lines[it]
            val line = lineSupplier.get()
            val score = objective!!.getScore(
                LegacyComponentSerializer.legacySection()
                    .serialize(
                        MiniMessage.miniMessage()
                            .deserialize(line)
                    ) + "\u00A7".repeat(it)
            )

            score.score = lines.size - it
        }
    }

    fun addPlayer(player: SpigotPlayer) {
        if (player in players) {
            return
        }

        players.add(player)
        update()
    }

    fun removePlayer(player: SpigotPlayer) {
        if (player !in players) {
            return
        }

        players.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager()!!.mainScoreboard

        update()
    }
}