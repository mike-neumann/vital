package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import java.util.function.Supplier

class VitalGlobalScoreboard(title: String, vararg var lines: Supplier<String>) : VitalScoreboard {
    val scoreboardContent = VitalScoreboardContent(title)
    private val _players = mutableListOf<SpigotPlayer>()
    val players: List<SpigotPlayer> get() = _players

    private fun update(player: SpigotPlayer) {
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        player.scoreboard = scoreboardContent.bukkitScoreboard
    }

    fun update() {
        updateContent()

        for (player in _players) {
            update(player)
        }
    }

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

    fun addPlayer(player: SpigotPlayer) {
        if (player in _players) return
        _players.add(player)
        update()
    }

    fun removePlayer(player: SpigotPlayer) {
        if (player !in _players) return
        _players.remove(player)
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        update()
    }
}