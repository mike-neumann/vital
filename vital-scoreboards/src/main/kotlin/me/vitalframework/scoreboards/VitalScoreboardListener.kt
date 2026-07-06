package me.vitalframework.scoreboards

import me.vitalframework.SpigotEventHandler
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Internal listener; removes a player's scoreboard when he leaves the server.
 */
class VitalScoreboardListener {
    @SpigotEventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        e.player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }
}
