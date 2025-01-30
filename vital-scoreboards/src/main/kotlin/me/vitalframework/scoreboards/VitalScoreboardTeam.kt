package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.bukkit.scoreboard.Team.OptionStatus

/**
 * Represents a team within a scoreboard in the Vital plugin framework.
 * This class manages team properties, members, and updates.
 */
class VitalScoreboardTeam internal constructor(
    val name: String,
    scoreboard: Scoreboard,
) {
    val players = mutableListOf<SpigotPlayer>()
    val options = mutableMapOf<Team.Option, OptionStatus>()
    val bukkitTeam: Team = scoreboard.registerNewTeam(
        PlainTextComponentSerializer.plainText()
            .serialize(
                LegacyComponentSerializer.legacySection()
                    .deserialize(name)
            )
    )
    var prefix: String? = null
    var suffix: String? = null
    var friendlyFire = false
    var canSeeFriendlyInvisibles = false

    /**
     * Updates the properties and members of this scoreboard team.
     */
    fun update() {
        bukkitTeam.displayName = name
        bukkitTeam.setAllowFriendlyFire(friendlyFire)
        bukkitTeam.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles)

        prefix?.let {
            bukkitTeam.prefix =
                LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(it))
        }

        suffix?.let {
            bukkitTeam.suffix =
                LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(it))
        }

        // Update all options
        options.forEach { (option, status) ->
            bukkitTeam.setOption(option, status)
        }

        // Clear all members
        bukkitTeam.entries.forEach {
            bukkitTeam.removeEntry(it)
        }

        // Add new members
        players.forEach {
            bukkitTeam.addPlayer(it)
        }
    }

    fun setOption(option: Team.Option, status: OptionStatus) {
        options[option] = status
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
        update()
    }
}