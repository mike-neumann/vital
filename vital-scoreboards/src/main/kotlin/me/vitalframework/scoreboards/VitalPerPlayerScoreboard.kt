package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import java.util.function.Function

/**
 * Represents a per-player scoreboard that can have individual contents for each player.
 * This class allows you to manage player-specific scoreboards with customizable titles and lines.
 */
class VitalPerPlayerScoreboard(
    val title: String,
    vararg var lines: Function<SpigotPlayer, String>,
) : VitalScoreboard {
    val vitalScoreboardContentMap = mutableMapOf<SpigotPlayer, VitalScoreboardContent>()

    /**
     * updates the users specified scoreboard
     *
     * @param player the player
     */
    fun update(player: SpigotPlayer) {
        if (!vitalScoreboardContentMap.containsKey(player)) {
            return
        }

        player.scoreboard = Bukkit.getScoreboardManager()!!.mainScoreboard
        updateContent(player)

        val scoreboard = vitalScoreboardContentMap[player]!!

        player.scoreboard = scoreboard.bukkitScoreboard
    }

    private fun updateContent(player: SpigotPlayer) {
        if (!vitalScoreboardContentMap.containsKey(player)) {
            return
        }

        val scoreboard = vitalScoreboardContentMap[player]!!

        scoreboard.update()

        val objective = scoreboard.bukkitScoreboard.getObjective(
            PlainTextComponentSerializer.plainText()
                .serialize(
                    LegacyComponentSerializer.legacySection()
                        .deserialize(scoreboard.title)
                )
        )
        val lines = applyLines(player)

        lines.indices.forEach {
            val score = objective!!.getScore(
                LegacyComponentSerializer.legacySection()
                    .serialize(
                        MiniMessage.miniMessage()
                            .deserialize(lines[it])
                    ) + "\u00A7".repeat(it)
            )

            score.score = lines.size - it
        }
    }

    fun addPlayer(player: SpigotPlayer) {
        if (vitalScoreboardContentMap.containsKey(player)) {
            return
        }

        vitalScoreboardContentMap.put(player, VitalScoreboardContent(title))
        update(player)
    }

    fun removePlayer(player: SpigotPlayer) {
        if (!vitalScoreboardContentMap.containsKey(player)) {
            return
        }

        vitalScoreboardContentMap.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager()!!.mainScoreboard
    }

    private fun applyLines(player: SpigotPlayer): List<String> = lines.map { it.apply(player) }
}