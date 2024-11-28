package me.vitalframework.scoreboards;

import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the content of a scoreboard in the Vital plugin framework.
 * This class manages the scoreboard's title, teams, and updates.
 *
 * @author xRa1ny
 */
@Getter
final class VitalScoreboardContent {
    /**
     * The Bukkit scoreboard instance associated with this content.
     */
    @NonNull
    private final Scoreboard bukkitScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

    /**
     * The scoreboard teams belonging to this content.
     */
    @NonNull
    private final List<VitalScoreboardTeam> teams = new ArrayList<>();

    /**
     * The title of this scoreboard content.
     */
    @NonNull
    private String title;

    /**
     * Creates a new VitalScoreboardContent with the specified title.
     *
     * @param title The title of the scoreboard content.
     */
    VitalScoreboardContent(@NonNull String title) {
        this.title = title;
    }

    /**
     * Updates the scoreboard content, including its title and teams.
     */
    public void update() {
        var objective = bukkitScoreboard.getObjective(PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacySection()
                        .deserialize(title)));

        if (objective == null) {
            objective = bukkitScoreboard.registerNewObjective(PlainTextComponentSerializer.plainText()
                            .serialize(LegacyComponentSerializer.legacySection()
                                    .deserialize(title)),
                    Criteria.DUMMY,
                    LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title)));
        }

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(title)));

        // Reset scores for existing entries
        for (var entry : bukkitScoreboard.getEntries()) {
            bukkitScoreboard.resetScores(entry);
        }

        // Update each scoreboard team
        for (var team : teams) {
            team.update();
        }
    }

    /**
     * Sets the title of this scoreboard content and triggers an update.
     *
     * @param title The new title for the scoreboard content.
     */
    public void setTitle(@NonNull String title) {
        this.title = title;
        update();
    }

    /**
     * Adds a scoreboard team to this scoreboard content.
     *
     * @param team The scoreboard team to add.
     */
    public void addTeam(@NonNull VitalScoreboardTeam team) {
        if (teams.contains(team)) {
            return;
        }

        teams.add(team);
        update();
    }

    /**
     * Removes a scoreboard team from this scoreboard content.
     *
     * @param team The scoreboard team to remove.
     */
    public void removeTeam(@NonNull VitalScoreboardTeam team) {
        if (!teams.contains(team)) {
            return;
        }

        teams.remove(team);
        update();
    }
}