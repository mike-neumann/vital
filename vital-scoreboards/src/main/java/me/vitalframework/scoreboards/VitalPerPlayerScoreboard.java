package me.vitalframework.scoreboards;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

/**
 * Represents a per-player scoreboard that can have individual contents for each player.
 * This class allows you to manage player-specific scoreboards with customizable titles and lines.
 *
 * @author xRa1ny
 */
public class VitalPerPlayerScoreboard implements VitalScoreboard {
    /**
     * the title of this per player scoreboard
     */
    @Getter
    @NonNull
    private final String title;
    /**
     * the scoreboard contents for each member of this per player scoreboard
     */
    @Getter
    @NonNull
    private final Map<Player, VitalScoreboardContent> vitalScoreboardContentMap = new HashMap<>();
    /**
     * the lines of this per player scoreboard
     */
    @Getter
    @NonNull
    private List<Function<Player, String>> lineList;

    /**
     * Constructs a new per player scoreboard instance with the specified title and lines.
     *
     * @param title    The title to display.
     * @param lineList The {@link Function} that defines the lines each player should see on the scoreboard when it's rendered. (may contain player relevant information and be different for each player).
     */
    @SneakyThrows
    @SafeVarargs
    public VitalPerPlayerScoreboard(@NonNull String title, @NonNull Function<Player, String>... lineList) {
        this.title = title;
        this.lineList = Arrays.asList(lineList);
    }

    /**
     * sets the lines of this per player scoreboard
     *
     * @param lineList the lines
     */
    @SafeVarargs
    public final void setLineList(@NonNull Function<Player, String>... lineList) {
        this.lineList = List.of(lineList);
    }

    /**
     * updates the users specified scoreboard
     *
     * @param player the player
     */
    public void update(@NonNull Player player) {
        if (!vitalScoreboardContentMap.containsKey(player)) {
            return;
        }

        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        updateContent(player);

        final var scoreboard = vitalScoreboardContentMap.get(player);

        player.getPlayer().setScoreboard(scoreboard.getBukkitScoreboard());
    }

    private void updateContent(@NonNull Player player) {
        if (!vitalScoreboardContentMap.containsKey(player)) {
            return;
        }

        final var scoreboard = vitalScoreboardContentMap.get(player);

        scoreboard.update();

        final var objective = scoreboard.getBukkitScoreboard().getObjective(PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacySection()
                        .deserialize(scoreboard.getTitle())));
        final var lines = applyLines(player);

        for (var i = 0; i < lines.size(); i++) {
            final var score = objective.getScore(LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage()
                            .deserialize(lines.get(i))) + "\u00A7".repeat(i));

            score.setScore(lines.size() - i);
        }
    }

    /**
     * adds the player specified to this per player scoreboard
     *
     * @param player the player
     */
    public void addPlayer(@NonNull Player player) {
        if (vitalScoreboardContentMap.containsKey(player)) {
            return;
        }

        vitalScoreboardContentMap.put(player, new VitalScoreboardContent(title));
        update(player);
    }

    /**
     * removes the player specified from this per player scoreboard
     *
     * @param player the player
     */
    public void removePlayer(@NonNull Player player) {
        if (!vitalScoreboardContentMap.containsKey(player)) {
            return;
        }

        vitalScoreboardContentMap.remove(player);
        player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @NonNull
    private List<String> applyLines(@NonNull Player player) {
        final var lines = new ArrayList<String>();

        for (var line : lineList) {
            lines.add(line.apply(player));
        }

        return lines;
    }
}