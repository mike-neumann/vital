package me.vitalframework.scoreboards;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Represents a team within a scoreboard in the Vital plugin framework.
 * This class manages team properties, members, and updates.
 *
 * @author xRa1ny
 */
public class VitalScoreboardTeam {
    /**
     * The name of this scoreboard team.
     */
    @Getter
    @NonNull
    private final String name;

    /**
     * The members of this scoreboard team.
     */
    @Getter
    @NonNull
    private final List<Player> playerList = new ArrayList<>();

    /**
     * The team options for this scoreboard team.
     */
    @Getter
    @NonNull
    private final Map<Team.Option, Team.OptionStatus> options = new HashMap<>();

    /**
     * The Bukkit team instance representing this scoreboard team.
     */
    @Getter
    @NonNull
    private final Team bukkitTeam;

    /**
     * The prefix of this scoreboard team.
     */
    @Getter
    @Setter
    private String prefix;

    /**
     * The suffix of this scoreboard team.
     */
    @Getter
    @Setter
    private String suffix;

    /**
     * Whether friendly fire is allowed for this team.
     */
    @Getter
    @Setter
    private boolean friendlyFire;

    /**
     * When true, members of this team can see friendly invisibles.
     */
    @Getter
    @Setter
    private boolean canSeeFriendlyInvisibles;

    /**
     * Creates a new VitalScoreboardTeam with the specified name and associated scoreboard content.
     *
     * @param name The name of the team.
     */
    VitalScoreboardTeam(@NonNull String name, @NonNull Scoreboard scoreboard) {
        this.name = name;
        bukkitTeam = scoreboard.registerNewTeam(PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacySection()
                        .deserialize(name)));
    }

    /**
     * Updates the properties and members of this scoreboard team.
     */
    public void update() {
        bukkitTeam.setDisplayName(name);
        bukkitTeam.setAllowFriendlyFire(friendlyFire);
        bukkitTeam.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles);

        if (prefix != null) {
            bukkitTeam.setPrefix(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(prefix)));
        }

        if (suffix != null) {
            bukkitTeam.setSuffix(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(suffix)));
        }

        // Update all options
        for (Map.Entry<Team.Option, Team.OptionStatus> entry : options.entrySet()) {
            final Team.Option option = entry.getKey();
            final Team.OptionStatus status = entry.getValue();

            bukkitTeam.setOption(option, status);
        }

        // Clear all members
        final Set<String> entries = bukkitTeam.getEntries();

        for (String entry : entries) {
            bukkitTeam.removeEntry(entry);
        }

        // Add new members
        for (Player player : playerList) {
            bukkitTeam.addPlayer(player);
        }
    }

    /**
     * Sets the option of this scoreboard team to the specified status.
     *
     * @param option The team option.
     * @param status The team option status.
     */
    public void setOption(@NonNull Team.Option option, @NonNull Team.OptionStatus status) {
        options.put(option, status);
    }

    /**
     * Adds a player to this scoreboard team.
     *
     * @param player The player to add.
     */
    public void addPlayer(@NonNull Player player) {
        if (playerList.contains(player)) {
            return;
        }

        playerList.add(player);
        update();
    }

    /**
     * Removes a player from this scoreboard team.
     *
     * @param player The player to remove.
     */
    public void removePlayer(@NonNull Player player) {
        if (!playerList.contains(player)) {
            return;
        }

        playerList.remove(player);
        update();
    }
}