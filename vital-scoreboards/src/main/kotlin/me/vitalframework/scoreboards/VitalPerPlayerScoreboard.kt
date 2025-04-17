package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import java.util.function.Function

class VitalPerPlayerScoreboard(val title: String, vararg var lines: Function<SpigotPlayer, String>) : VitalScoreboard {
    private val _scoreboardContent = mutableMapOf<SpigotPlayer, VitalScoreboardContent>()
    val scoreboardContent: Map<SpigotPlayer, VitalScoreboardContent> get() = _scoreboardContent

    fun update(player: SpigotPlayer) {
        if (!_scoreboardContent.containsKey(player)) return
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        updateContent(player)
        player.scoreboard = _scoreboardContent[player]!!.bukkitScoreboard
    }

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

    fun addPlayer(player: SpigotPlayer) {
        if (_scoreboardContent.containsKey(player)) return
        _scoreboardContent[player] = VitalScoreboardContent(title)
        update(player)
    }

    fun removePlayer(player: SpigotPlayer) {
        if (!_scoreboardContent.containsKey(player)) return
        _scoreboardContent.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }

    private fun applyLines(player: SpigotPlayer) = lines.map { it.apply(player) }
}