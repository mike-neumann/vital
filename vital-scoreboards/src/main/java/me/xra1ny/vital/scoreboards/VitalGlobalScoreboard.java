package me.xra1ny.vital.scoreboards;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Manages a global scoreboard displayed to multiple players.
 * This class allows the creation and management of scoreboards that can be displayed to multiple players simultaneously.
 *
 * @author xRa1ny
 */
public class VitalGlobalScoreboard extends VitalScoreboard {
    /**
     * The scoreboard content of this global scoreboard.
     */
    @Getter
    @NonNull
    private final VitalScoreboardContent vitalScoreboardContent;
    /**
     * The list of users associated with this global scoreboard.
     */
    @Getter
    @NonNull
    private final List<Player> playerList = new ArrayList<>();
    /**
     * The lines of this global scoreboard.
     */
    @Getter
    @NonNull
    private List<Supplier<String>> lines;

    /**
     * Creates a new instance of the VitalGlobalScoreboard class.
     *
     * @param title The title of the scoreboard.
     * @param lines The lines of the scoreboard.
     */
    @SafeVarargs
    @SneakyThrows
    public VitalGlobalScoreboard(@NonNull String title, @NonNull Supplier<String>... lines) {
        vitalScoreboardContent = new VitalScoreboardContent(title);
        this.lines = Arrays.asList(lines);
    }

    /**
     * Sets the lines of this global scoreboard.
     *
     * @param lines The lines to set.
     */
    public void setLines(@NonNull Supplier<String>... lines) {
        this.lines = List.of(lines);
    }

    /**
     * Updates the scoreboard for a specific player.
     *
     * @param player The player for whom to update the scoreboard.
     */
    private void update(@NonNull Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        player.setScoreboard(vitalScoreboardContent.getBukkitScoreboard());
    }

    /**
     * Updates the scoreboards for all users associated with this global scoreboard.
     */
    public void update() {
        updateContent();

        for (Player player : playerList) {
            update(player);
        }
    }

    /**
     * Updates the content of this scoreboard, including titles and scores.
     */
    public void updateContent() {
        vitalScoreboardContent.update();

        final Objective objective = vitalScoreboardContent.getBukkitScoreboard().getObjective(PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacySection()
                        .deserialize(vitalScoreboardContent.getTitle())));

        for (int i = 0; i < lines.size(); i++) {
            final Supplier<String> lineSupplier = lines.get(i);
            final String line = lineSupplier.get();
            final Score score = objective.getScore(LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage()
                            .deserialize(line)) + "\u00A7".repeat(i));

            score.setScore(lines.size() - i);
        }
    }

    /**
     * Adds a player to this global scoreboard.
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
     * Removes a player from this global scoreboard.
     *
     * @param player The player to remove.
     */
    @SuppressWarnings("DataFlowIssue")
    public void removePlayer(@NonNull Player player) {
        if (!playerList.contains(player)) {
            return;
        }

        playerList.remove(player);
        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        update();
    }
}