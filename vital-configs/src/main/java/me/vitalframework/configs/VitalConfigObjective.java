package me.vitalframework.configs;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

/**
 * Wrapper class to store scoreboard data to a config file.
 *
 * @author xRa1ny
 */
public class VitalConfigObjective {
    /**
     * the criteria in string format
     */
    @VitalConfig.Property(String.class)
    public String criteria;

    /**
     * the display slot of the objective
     */
    @VitalConfig.Property(DisplaySlot.class)
    public DisplaySlot slot;

    /**
     * the actual title
     */
    @VitalConfig.Property(String.class)
    public String title;

    /**
     * all displayed lines
     */
    @VitalConfig.Property(String.class)
    public String[] lines;

    /**
     * Converts a bukkit objective into a config objective used for config serialization
     *
     * @param objective The bukkit objective
     * @return The config objective instance
     */
    @NonNull
    public static VitalConfigObjective of(@NonNull Objective objective) {
        final var vitalConfigObjective = new VitalConfigObjective();

        vitalConfigObjective.criteria = objective.getCriteria();
        vitalConfigObjective.slot = objective.getDisplaySlot();
        vitalConfigObjective.title = objective.getName();
        vitalConfigObjective.lines = objective.getScoreboard().getEntries().toArray(String[]::new);

        return vitalConfigObjective;
    }

    /**
     * Converts this config objective back into a bukkit one
     *
     * @return The bukkit objective
     */
    @NonNull
    public Objective toObjective() {
        final var scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        final var objective = scoreboard.registerNewObjective(title, criteria, title);

        objective.setDisplaySlot(slot);

        // set lines...
        for (var i = 0; i < lines.length; i++) {
            final var line = lines[i];

            objective.getScore(line).setScore(i);
        }

        return objective;
    }
}