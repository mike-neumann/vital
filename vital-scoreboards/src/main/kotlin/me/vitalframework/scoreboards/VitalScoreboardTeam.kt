package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.bukkit.scoreboard.Team.OptionStatus

class VitalScoreboardTeam internal constructor(val name: String, scoreboard: Scoreboard) {
    private val _players = mutableListOf<SpigotPlayer>()
    val players: List<SpigotPlayer> get() = _players
    private val _options = mutableMapOf<Team.Option, OptionStatus>()
    val options: Map<Team.Option, OptionStatus> get() = _options
    val bukkitTeam: Team = scoreboard.registerNewTeam(
        PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(name))
    )
    var prefix: String? = null
    var suffix: String? = null
    var friendlyFire = false
    var canSeeFriendlyInvisibles = false

    fun update() {
        bukkitTeam.displayName = name
        bukkitTeam.setAllowFriendlyFire(friendlyFire)
        bukkitTeam.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles)

        if (prefix != null) bukkitTeam.prefix =
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(prefix!!))
        if (suffix != null) bukkitTeam.suffix =
            LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(suffix!!))
        // Update all options
        for ((option, status) in _options) {
            bukkitTeam.setOption(option, status)
        }
        // Clear all members
        for (entry in bukkitTeam.entries) {
            bukkitTeam.removeEntry(entry)
        }
        // Add new members
        for (player in _players) {
            bukkitTeam.addPlayer(player)
        }
    }

    fun setOption(option: Team.Option, status: OptionStatus) = run { _options[option] = status }

    fun addPlayer(player: SpigotPlayer) {
        if (player in _players) return

        _players.add(player)
        update()
    }

    fun removePlayer(player: SpigotPlayer) {
        if (player !in _players) return

        _players.remove(player)
        update()
    }
}