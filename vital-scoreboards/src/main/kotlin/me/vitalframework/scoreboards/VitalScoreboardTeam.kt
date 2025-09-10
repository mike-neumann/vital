package me.vitalframework.scoreboards

import me.vitalframework.SpigotPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.bukkit.scoreboard.Team.OptionStatus
import java.util.UUID

/**
 * Represents a team within a scoreboard system, providing functionality for managing
 * players, team options, and interaction with the underlying Bukkit API.
 *
 * This class encapsulates team-related data and operations, such as adding/removing
 * players, configuring team options, and synchronizing the team's state with the server.
 *
 * Key attributes of the team include:
 * - Display name management (prefix and suffix).
 * - Configuration of friendly fire behavior and invisibility options.
 * - Integration with the Bukkit scoreboard API, ensuring consistency in team representation.
 *
 * The `VitalScoreboardTeam` is primarily used in the context of scoreboards and allows
 * for fine-grained control over team behaviors, visibility, and membership.
 *
 * @constructor Creates a new instance of a `VitalScoreboardTeam` with a specified name and associated scoreboard.
 * @param name The unique name of the team, used as an identifier.
 * @param scoreboard The scoreboard to which this team belongs.
 */
class VitalScoreboardTeam internal constructor(
    val name: String,
    scoreboard: Scoreboard,
) {
    private val _players = mutableListOf<UUID>()

    /**
     * Returns a read-only list of UUIDs representing the players currently associated with the team.
     *
     * This property provides access to the internal `_players` list, which contains the unique
     * identifiers of all players that are members of the team. The list is used to track membership
     * and to perform operations that require synchronization with the team's state.
     *
     * Modifications to the list of players, such as adding or removing members, should only be performed
     * using the dedicated methods of the containing `VitalScoreboardTeam` class to ensure consistency
     * with the team's configuration and behavior.
     *
     * The list is primarily used in processes such as updating the scoreboard and team configurations.
     */
    val players: List<UUID>
        get() = _players

    private val _options = mutableMapOf<Team.Option, OptionStatus>()

    /**
     * Represents the behavioral and configuration options associated with the team,
     * mapped to their corresponding statuses.
     *
     * The `options` property provides a read-only view of the team's options and their current states.
     *
     * Key points about this property:
     * - The map keys are instances of `Team.Option`, which list the available configurable options for the team.
     * - The map values are instances of `OptionStatus`, representing the current state of the respective option.
     * - Used internally to manage and synchronize the behavior and settings of the team, including properties like
     *   friendly fire, visibility, and other configurable attributes.
     *
     * This property is used during team updates to ensure the team's internal and external representations
     * are consistent with the configured options.
     */
    val options: Map<Team.Option, OptionStatus>
        get() = _options

    /**
     * Represents the underlying Bukkit team instance associated with the `VitalScoreboardTeam`.
     *
     * This variable is responsible for encapsulating the Bukkit team object and acts as the bridge
     * between the internal team structure of the `VitalScoreboardTeam` and the underlying Bukkit API.
     *
     * The `bukkitTeam` is registered when the `VitalScoreboardTeam` is initialized, ensuring that the
     * team is correctly represented within the Bukkit system. It uses the serialized name of the
     * `VitalScoreboardTeam` as its identifier while deserializing any legacy component formats.
     *
     * Key purposes of this variable include:
     * - Managing the state and attributes of the team related to the Bukkit API.
     * - Facilitating synchronization of team configurations such as options, members, and display properties.
     * - Interacting with the Minecraft server through the Bukkit scoreboard system.
     */
    val bukkitTeam: Team =
        scoreboard.registerNewTeam(
            PlainTextComponentSerializer.plainText().serialize(LegacyComponentSerializer.legacySection().deserialize(name)),
        )

    /**
     * Represents the prefix applied to the display names of members in this team.
     *
     * The prefix is displayed before the player's name in the scoreboard or chat,
     * depending on the team configuration and server settings. It is commonly used
     * to visually categorize or identify players as part of the team.
     *
     * This property may be nullable, indicating that no prefix is set for the team.
     * If modified, the changes will reflect when the team is updated.
     */
    var prefix: String? = null

    /**
     * Represents the suffix applied to the team name in the scoreboard.
     *
     * The suffix is appended to the team's display name when shown on the scoreboard.
     * It is used to visually distinguish or add additional information to the team's name.
     *
     * A value of `null` indicates that no suffix is currently applied.
     */
    var suffix: String? = null

    /**
     * Indicates whether friendly fire is allowed within the team.
     *
     * When set to `true`, members of the team can inflict damage on each other.
     * When set to `false`, friendly fire is disabled, preventing teammates from harming one another.
     *
     * This property is used to configure team behavior and synchronize it with
     * relevant settings in the underlying scoreboard or game configuration.
     */
    var friendlyFire = false

    /**
     * Indicates whether the team members can see other friendly players who are invisible.
     *
     * When set to `true`, this property ensures that members of the same team
     * are able to see each other, even if some players have invisibility status
     * applied. This is primarily used to enhance coordination and visibility
     * within friendly teams during gameplay.
     *
     * By default, this value is set to `false`, meaning invisible players remain
     * unseen to their teammates unless explicitly configured otherwise.
     */
    var canSeeFriendlyInvisibles = false

    /**
     * Updates the current state of the team by synchronizing its settings and members with the
     * internal configuration. This includes updating display properties, behavioral options,
     * and associated players.
     *
     * Operations performed by this method include:
     * - Setting the display name of the team using the `name` field.
     * - Updating the friendly fire and invisibility settings.
     * - Applying prefixes and suffixes to the team if specified.
     * - Syncing behavioral options for the team with the `_options` map.
     * - Clearing all current team members and re-adding members from the `_players` list.
     *
     * This method ensures that the Bukkit team representation reflects the current state
     * of the `VitalScoreboardTeam` instance.
     */
    fun update() {
        bukkitTeam.displayName(MiniMessage.miniMessage().deserialize(name))
        bukkitTeam.setAllowFriendlyFire(friendlyFire)
        bukkitTeam.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles)

        if (prefix != null) {
            bukkitTeam.prefix(MiniMessage.miniMessage().deserialize(prefix!!))
        }

        if (suffix != null) {
            bukkitTeam.suffix(MiniMessage.miniMessage().deserialize(suffix!!))
        }
        // Update all options
        for ((option, status) in _options) {
            bukkitTeam.setOption(option, status)
        }
        // Clear all members
        for (entry in bukkitTeam.entries) {
            bukkitTeam.removeEntry(entry)
        }
        // Add new members
        for (uniqueId in _players) {
            val player = Bukkit.getPlayer(uniqueId) ?: continue
            bukkitTeam.addPlayer(player)
        }
    }

    /**
     * Updates the specified option's status for this team.
     *
     * This method modifies the internal state of the team by associating the given
     * option with the provided status. It updates the options map to reflect
     * the new configuration.
     *
     * @param option The option to be set or updated for this team.
     * @param status The status that should be assigned to the specified option.
     */
    fun setOption(
        option: Team.Option,
        status: OptionStatus,
    ) {
        _options[option] = status
    }

    /**
     * Adds a player to the team. If the player is already part of the team, no action is taken.
     * The scoreboard is updated after successfully adding the player.
     *
     * @param player The player to be added to the team.
     */
    fun addPlayer(player: SpigotPlayer) {
        if (player.uniqueId in _players) return
        _players.add(player.uniqueId)
        update()
    }

    /**
     * Removes the specified player from the team's player list. If the player is not part of the team,
     * the method does nothing. Updates the team state after the player is removed.
     *
     * @param player The player to be removed from the team.
     */
    fun removePlayer(player: SpigotPlayer) {
        if (player.uniqueId !in _players) return
        _players.remove(player.uniqueId)
        update()
    }
}
