package me.xra1ny.vital.utils;

import lombok.NonNull;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.kyori.adventure.text.Component.empty;

/**
 * Utility class for operations many developers might find useful.
 *
 * @author xRa1ny
 * @apiNote This class can be used standalone, detached from any Vital project. It only contains utilities for easier interaction with the SpigotAPI.
 */
public interface VitalUtils<Player> {
    static Spigot spigot() {
        return new Spigot();
    }

    static Bungeecord bungeecord() {
        return new Bungeecord();
    }

    static String chatButton(String color, String hover, String text, String click, ClickEvent.Action action) {
        return "<hover:show_text:'" + hover + "'>" +
                "<click:" + action.name().toLowerCase() + ":'" + click + "'>" +
                "<" + color + ">" + "[" + text + "]" +
                "</click>" +
                "</hover>";
    }

    static String chatRunCommandButton(String color, String text, String command) {
        return chatButton(color, command, text, command, ClickEvent.Action.RUN_COMMAND);
    }

    static String chatSuggestCommandButton(String color, String text, String command) {
        return chatButton(color, command, text, command, ClickEvent.Action.SUGGEST_COMMAND);
    }

    static String chatRunCommandYesButton(String command) {
        return chatRunCommandButton("green", "YES", command);
    }

    static String chatRunCommandNoButton(String command) {
        return chatRunCommandButton("red", "NO", command);
    }

    static String chatRunCommandOkButton(String command) {
        return chatRunCommandButton("green", "OK", command);
    }

    static String chatRunCommandXButton(String command) {
        return chatRunCommandButton("red", "X", command);
    }

    /**
     * Checks if the given {@link Material} type is valid for placement in midair.
     *
     * @param material The {@link Material} type.
     * @return true if the type can be placed in midair; false otherwise.
     */
    static boolean canBePlacedInMidAir(@NonNull Material material) {
        return !material.hasGravity() &&
                !isVegetation(material) &&
                (material != Material.REDSTONE &&
                        material != Material.REDSTONE_TORCH &&
                        material != Material.REPEATER &&
                        material != Material.COMPARATOR &&
                        material != Material.LEVER &&
                        material != Material.TRIPWIRE &&
                        !material.name().contains("BUTTON") &&
                        !material.name().contains("PRESSURE_PLATE") &&
                        !material.name().contains("RAIL"));
    }

    /**
     * Checks if the given {@link Material} type is vegetation or not.
     *
     * @param material The {@link Material} type.
     * @return true if the given type is vegetation; false otherwise.
     */
    static boolean isVegetation(@NonNull Material material) {
        return material.name().contains("SAPLING") ||
                material.name().contains("FLOWER") ||
                material.name().contains("WHEAT") ||
                material.name().contains("SEEDS") ||
                material.name().contains("CROP") ||
                material.name().contains("KELP") ||
                material.name().contains("BUSH") ||
                material.name().contains("MUSHROOM") ||
                material.name().contains("CHORUS") ||
                material.name().contains("FERN") ||
                material.name().contains("POTTED") ||
                material.name().contains("ROSE") ||
                material.name().contains("POPPY") ||
                material == Material.MELON_STEM ||
                material == Material.PUMPKIN_STEM ||
                material == Material.BAMBOO ||
                material == Material.SUGAR_CANE ||
                material == Material.SEA_PICKLE ||
                material == Material.NETHER_WART ||
                material == Material.LILY_PAD ||
                material == Material.VINE ||
                material == Material.GLOW_LICHEN ||
                material == Material.SCULK_VEIN ||
                material == Material.CACTUS ||
                material == Material.LILAC ||
                material == Material.PEONY ||
                material == Material.TALL_GRASS ||
                material == Material.TALL_SEAGRASS ||
                material == Material.MANGROVE_PROPAGULE;
    }

    /**
     * Checks if the given {@link Material} type is a redstone machine like, redstone torch, piston, comparator, etc.
     *
     * @param material The {@link Material} type.
     * @return true if the type is a redstone machine; false otherwise.
     */
    static boolean isRedstoneMachine(@NonNull Material material) {
        return material.getCreativeCategory() == CreativeCategory.REDSTONE && (
                material == Material.REDSTONE_TORCH ||
                        material.name().contains("PISTON") ||
                        material.name().contains("BUTTON") ||
                        material.name().contains("PRESSURE_PLATE") ||
                        material.name().contains("DETECTOR") ||
                        material.name().contains("LAMP") ||
                        material == Material.COMPARATOR ||
                        material == Material.REPEATER ||
                        material == Material.REDSTONE ||
                        material == Material.REDSTONE_WIRE ||
                        material == Material.OBSERVER ||
                        material == Material.DROPPER ||
                        material == Material.DISPENSER ||
                        material == Material.HOPPER ||
                        material == Material.HOPPER_MINECART);
    }

    /**
     * Checks if the given location is contained within the mapped location1 and location2 area.
     *
     * @param location1 The first location.
     * @param location2 The second location.
     * @param location  The location to check if it is contained within the area.
     * @return true if the location is within the area; false otherwise.
     */
    static boolean isInsideLocationArea(@NonNull Location location1, @NonNull Location location2, @NonNull Location location) {
        final double ourMinX = Math.min(location1.getX(), location2.getX());
        final double ourMaxX = Math.max(location1.getX(), location2.getX());

        final double ourMinY = Math.min(location1.getY(), location2.getY());
        final double ourMaxY = Math.max(location1.getY(), location2.getY());

        final double ourMinZ = Math.min(location1.getZ(), location2.getZ());
        final double ourMaxZ = Math.max(location1.getZ(), location2.getZ());

        final double theirX = location.getX();
        final double theirY = location.getY();
        final double theirZ = location.getZ();

        return theirX >= ourMinX && theirX <= ourMaxX &&
                theirY >= ourMinY && theirY <= ourMaxY &&
                theirZ >= ourMinZ && theirZ <= ourMaxZ;
    }

    /**
     * Gets a random location within the mapped location1 and location2 area.
     *
     * @param location1 The first location.
     * @param location2 The second location.
     * @return The randomly calculated area location.
     */
    @NonNull
    static Location getRandomLocationInLocationArea(@NonNull Location location1, @NonNull Location location2) {
        final double ourMinX = Math.min(location1.getX(), location2.getX());
        final double ourMaxX = Math.max(location1.getX(), location2.getX());

        final double ourMinY = Math.min(location1.getY(), location2.getY());
        final double ourMaxY = Math.max(location1.getY(), location2.getY());

        final double ourMinZ = Math.min(location1.getZ(), location2.getZ());
        final double ourMaxZ = Math.max(location1.getZ(), location2.getZ());

        final double randomX = new Random().nextDouble(ourMinX, ourMaxX);
        final double randomY = new Random().nextDouble(ourMinY, ourMaxY);
        final double randomZ = new Random().nextDouble(ourMinZ, ourMaxZ);

        return new Location(location1.getWorld(), randomX, randomY, randomZ);
    }

    /**
     * Gets the centered offset block location of the given location.
     *
     * @param location The block location.
     * @param xOffset  The x offset.
     * @param yOffset  The y offset.
     * @param zOffset  The z offset.
     * @return The centered offset location.
     */
    @NonNull
    static Location getCenterBlockLocation(@NonNull Location location, double xOffset, double yOffset, double zOffset) {
        final Location finalLocation = location.getBlock().getLocation().clone()
                .add(.5, .5, .5)
                .add(xOffset, yOffset, zOffset);

        finalLocation.setPitch(location.getPitch());
        finalLocation.setYaw(location.getYaw());
        finalLocation.setDirection(location.getDirection());

        return finalLocation;
    }

    /**
     * Gets the center location of the targeted location block.
     *
     * @param location The location.
     * @return The centered block location.
     */
    @NonNull
    static Location getCenterBlockLocation(@NonNull Location location) {
        return getCenterBlockLocation(location, 0, 0, 0);
    }

    /**
     * Gets the top location of the targeted location block, while offsetting only the y-axis to be on top of the block.
     *
     * @param location The location.
     * @return The location.
     */
    @NonNull
    static Location getCenterBlockTopLocation(@NonNull Location location) {
        return getCenterBlockLocation(location, 0, .5, 0);
    }

    /**
     * Gets the horizontal centered location of the targeted location block, while offsetting only the x- and z-axis of the block.
     *
     * @param location The location.
     * @return The location.
     */
    @NonNull
    static Location getCenterBlockSideLocation(@NonNull Location location) {
        return getCenterBlockLocation(location, 0, -.5, 0);
    }

    /**
     * Takes the given world and "cleans" all rules for minigame or stale world purposes.
     *
     * @param world The world.
     * @apiNote Calling this method sets the following values:
     * <ul>
     *  <li>DO_DAYLIGHT_CYCLE       : false</li>
     *  <li>DO_FIRE_TICK            : false</li>
     *  <li>DO_MOB_SPAWNING         : false</li>
     *  <li>ANNOUNCE_ADVANCEMENTS   : false</li>
     *  <li>DO_MOB_LOOT             : false</li>
     *  <li>DO_MOB_SPAWNING         : false</li>
     *  <li>DO_TRADER_SPAWNING      : false</li>
     *  <li>DO_VINES_SPREAD         : false</li>
     *  <li>MOB_GRIEFING            : false</li>
     *  <li>SHOW_DEATH_MESSAGES     : false</li>
     *  <li>KEEP_INVENTORY          : true</li>
     *  <li>DISABLE_RAIDS           : true</li>
     *  <li>TIME                    : 0</li>
     *  <li>DIFFICULTY              : PEACEFUL</li>
     *  <li>DO_WEATHER_CYCLE        : false</li>
     *  <li>WEATHER_DURATION        : 0</li>
     * </ul>
     */
    static void cleanGameRules(@NonNull World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_VINES_SPREAD, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);

        world.setTime(0);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setWeatherDuration(0);
    }

    /**
     * Takes the given world by name and "cleans" all gamerules for minigame or clean world purposes.
     *
     * @param worldName The name of the world to clean.
     * @see VitalUtils#cleanGameRules(World) for more information about cleansed world rules.
     */
    static void cleanGameRules(@NonNull String worldName) {
        final World world = Optional.ofNullable(Bukkit.getWorld(worldName))
                .orElseThrow(() -> new RuntimeException("World %s does not exist"
                        .formatted(worldName)));

        cleanGameRules(world);
    }

    /**
     * Broadcasts an action to be performed for each player currently connected to this server.
     *
     * @param action          The action to perform for each player.
     * @param playerPredicate The {@link Predicate} specifying the condition in which each action is performed.
     */
    void broadcastAction(@NonNull Predicate<Player> playerPredicate, @NonNull Consumer<Player> action);

    /**
     * Broadcasts an action to be performed for each player currently connected to this server.
     *
     * @param action The action to perform for each player.
     */
    default void broadcastAction(@NonNull Consumer<Player> action) {
        broadcastAction(p -> true, action);
    }

    /**
     * Sends a message to the given player with set tag resolvers for minimessage support.
     *
     * @param player       The player.
     * @param message      The message.
     * @param tagResolvers The tag resolvers for custom minimessage support.
     */
    void sendMessage(@NonNull Player player, @NonNull String message, @Nullable TagResolver... tagResolvers);

    /**
     * Broadcasts a message to all players currently connected to the server, matching the given {@link Predicate}.
     *
     * @param message         The message to broadcast.
     * @param playerPredicate The Predicate specifying the condition in which the message should be broadcast.
     * @param tagResolvers    Any tag resolvers for custom minimessage tag syntax.
     */
    default void broadcastMessage(@NonNull String message, @NonNull Predicate<Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(playerPredicate, player -> sendMessage(player, message, tagResolvers));
    }

    /**
     * Broadcasts a message to all connected players on the server.
     *
     * @param message      The message to broadcast.
     * @param tagResolvers Any tag resolvers for custom minimessage tag syntax.
     * @apiNote The given message will be broadcast in minimessage syntax.
     */
    default void broadcastMessage(@NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastMessage(message, player -> true, tagResolvers);
    }

    /**
     * Sends a title to the given player in minimessage syntax with the specified tag resolvers for any custom minimessage tags for replacement.
     *
     * @param player       The player.
     * @param title        The title.
     * @param subtitle     The subtitle.
     * @param fadeIn       The fade in times (measured in ticks).
     * @param stay         The stay times (measured in ticks).
     * @param fadeOut      The fade out times (measured in ticks).
     * @param tagResolvers Any custom tag resolvers for custom minimessage tags.
     */
    void sendTitle(@NonNull Player player, @Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @Range(from = 0, to = 72_000) int stay, @Range(from = 0, to = 72_000) int fadeOut, @NonNull TagResolver @NonNull ... tagResolvers);

    /**
     * Sends a title to the given player in minimessage syntax with the specified tag resolvers for any custom minimessage tags for replacement.
     * With default fade times.
     *
     * @param player       The player.
     * @param title        The title.
     * @param subtitle     The subtitle.
     * @param tagResolvers Any custom tag resolvers for minimessage.
     */
    void sendTitle(@NonNull Player player, @Nullable String title, @Nullable String subtitle, @NonNull TagResolver @NonNull ... tagResolvers);

    /**
     * Broadcasts a title to all players currently connected to this server.
     *
     * @param title           The title to broadcast.
     * @param subtitle        The subtitle to broadcast.
     * @param fadeIn          The fade-in amount (in ticks).
     * @param stay            The stay amount (in ticks).
     * @param fadeOut         The fade-out amount (in ticks).
     * @param playerPredicate The {@link Predicate} specifying the condition in which the title is broadcast.
     * @param tagResolvers    Any tag resolvers for custom minimessage replacement syntax.
     */
    default void broadcastTitle(@Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @Range(from = 0, to = 72_000) int stay, @Range(from = 0, to = 72_000) int fadeOut, @NonNull Predicate<Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(playerPredicate, player -> sendTitle(player, title, subtitle, fadeIn, stay, fadeOut, tagResolvers));
    }

    /**
     * Broadcasts a title to all players currently connected to this server, matching the given {@link Predicate}
     *
     * @param title           The title to broadcast.
     * @param subtitle        The subtitle to broadcast.
     * @param playerPredicate The {@link Predicate} specifying the condition in which the title is broadcast.
     * @param tagResolvers    Any tag resolvers for custom minimessage tag syntax.
     */
    default void broadcastTitle(@Nullable String title, @Nullable String subtitle, @NonNull Predicate<Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(playerPredicate, player -> sendTitle(player, title, subtitle, tagResolvers));
    }

    /**
     * Broadcasts a title to all players currently connected to this server.
     *
     * @param title        The title to broadcast.
     * @param subtitle     The subtitle to broadcast.
     * @param tagResolvers Any tag resolvers for custom minimessage tag syntax.
     */
    default void broadcastTitle(@Nullable String title, @Nullable String subtitle, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(player -> sendTitle(player, title, subtitle, tagResolvers));
    }

    /**
     * Broadcasts a title to all players currently connected to this server.
     *
     * @param title        The title to broadcast.
     * @param subtitle     The subtitle to broadcast.
     * @param fadeIn       The fade-in amount (in ticks).
     * @param stay         The stay amount (in ticks).
     * @param fadeOut      The fade-out amount (in ticks).
     * @param tagResolvers Any tag resolvers for custom minimessage tag syntax.
     */
    default void broadcastTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(player -> sendTitle(player, title, subtitle, fadeIn, stay, fadeOut, tagResolvers));
    }

    /**
     * Sends a persistent (permanent) title to the given player in minimessage syntax with the specified tag resolvers for any custom minimessage tags for replacement.
     *
     * @param player       The player.
     * @param title        The title.
     * @param subtitle     The subtitle.
     * @param fadeIn       The fade in times (measured in ticks).
     * @param tagResolvers Any custom tag resolvers for custom minimessage tags.
     * @apiNote The title will stay approx. 1h
     */
    void sendPersistentTitle(@NonNull Player player, @Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @NonNull TagResolver @NonNull ... tagResolvers);

    /**
     * Broadcasts a persistent (permanent) title to all players in minimessage syntax with the specified predicate and tag resolvers for any custom minimessage tags for replacement.
     *
     * @param title           The title.
     * @param subtitle        The subtitle.
     * @param fadeIn          The fade in times (measured in ticks).
     * @param playerPredicate The predicate the player MUST MATCH when sending the title.
     * @param tagResolvers    Any custom tag resolvers for custom minimessage tags.
     * @apiNote The title will stay approx. 1h
     */
    default void broadcastPersistentTitle(@Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @NonNull Predicate<Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(playerPredicate, player -> sendPersistentTitle(player, title, subtitle, fadeIn, tagResolvers));
    }

    /**
     * Broadcasts a persistent (permanent) title to all players in minimessage syntax with the specified tag resolvers for any custom minimessage tags for replacement.
     *
     * @param title        The title.
     * @param subtitle     The subtitle.
     * @param fadeIn       The fade in times (measured in ticks).
     * @param tagResolvers Any custom tag resolvers for custom minimessage tags.
     * @apiNote The title will stay approx. 1h
     */
    default void broadcastPersistentTitle(@Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(player -> sendPersistentTitle(player, title, subtitle, fadeIn, tagResolvers));
    }

    /**
     * Sends an action bar message to the given player in minimessage syntax.
     *
     * @param player       The player.
     * @param message      The message in minimessage syntax.
     * @param tagResolvers Any custom tag resolver for minimessage tag syntax.
     */
    void sendActionBar(@NonNull Player player, @NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers);

    /**
     * Broadcasts an action bar message for all players in minimessage syntax.
     *
     * @param message         The message.
     * @param playerPredicate The predicate every player MUST MATCH WITH.
     * @param tagResolvers    Any custom tag resolvers for minimessage tag syntax.
     */
    default void broadcastActionBar(@NonNull String message, @NonNull Predicate<Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(playerPredicate, player -> sendActionBar(player, message, tagResolvers));
    }

    /**
     * Broadcasts an action bar message for all players in minimessage syntax.
     *
     * @param message      The message.
     * @param tagResolvers Any custom tag resolvers for minimessage tag syntax.
     */
    default void broadcastActionBar(@NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers) {
        broadcastAction(player -> sendActionBar(player, message, tagResolvers));
    }

    /**
     * The spigot implementation for every vital util.
     */
    class Spigot implements VitalUtils<org.bukkit.entity.Player> {
        @Override
        public void broadcastAction(@NonNull Predicate<org.bukkit.entity.Player> playerPredicate, @NonNull Consumer<org.bukkit.entity.Player> action) {
            Bukkit.getOnlinePlayers().stream()
                    .filter(playerPredicate)
                    .forEach(action);
        }

        @Override
        public void sendMessage(org.bukkit.entity.@NonNull Player player, @NonNull String message, @Nullable TagResolver... tagResolvers) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(message, tagResolvers == null ? new TagResolver[0] : tagResolvers));
        }

        @Override
        public void broadcastMessage(@NonNull String message, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastAction(playerPredicate, player -> player.sendRichMessage(message, tagResolvers));
        }

        @Override
        public void broadcastMessage(@NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastMessage(message, player -> true, tagResolvers);
        }

        /**
         * Broadcasts a {@link Sound} to all players currently connected to this server, matching the given {@link Predicate}.
         *
         * @param sound           The sound to broadcast.
         * @param volume          The volume of the sound.
         * @param pitch           The pitch of the sound.
         * @param playerPredicate The Predicate specifying the condition in which the sound is broadcast.
         */
        public void broadcastSound(@NonNull Sound sound, float volume, float pitch, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate) {
            broadcastAction(playerPredicate, player -> player.playSound(player, sound, volume, pitch));
        }

        /**
         * Broadcasts a {@link Sound} to all players currently connected to this server.
         *
         * @param sound  The sound to broadcast.
         * @param volume The volume of the sound.
         * @param pitch  The pitch of the sound.
         */
        public void broadcastSound(@NonNull Sound sound, float volume, float pitch) {
            broadcastSound(sound, volume, pitch, player -> true);
        }

        /**
         * Broadcasts a {@link Sound} to all players currently connected to this server, matching the given {@link Predicate}.
         *
         * @param sound           The sound to broadcast.
         * @param playerPredicate The Predicate specifying the condition in which the sound should be broadcast.
         */
        public void broadcastSound(@NonNull Sound sound, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate) {
            broadcastSound(sound, 1f, 1f, playerPredicate);
        }

        /**
         * Broadcasts a {@link Sound} to all players currently connected to this server.
         * volume: 1f, pitch: 1f.
         *
         * @param sound The sound to broadcast.
         */
        public void broadcastSound(@NonNull Sound sound) {
            broadcastSound(sound, player -> true);
        }

        @Override
        public void sendTitle(@NonNull org.bukkit.entity.Player player, @Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @Range(from = 0, to = 72_000) int stay, @Range(from = 0, to = 72_000) int fadeOut, @NonNull TagResolver @NonNull ... tagResolvers) {
            player.showTitle(Title.title(
                    title == null ? empty() : MiniMessage.miniMessage().deserialize(title, tagResolvers),
                    subtitle == null ? empty() : MiniMessage.miniMessage().deserialize(subtitle, tagResolvers),
                    Title.Times.times(Duration.ofMillis((long) ((fadeIn / 20f) * 1_000)), Duration.ofMillis((long) ((stay / 20f) * 1_000)), Duration.ofMillis((long) ((fadeOut / 20f) * 1_000)))
            ));
        }

        @Override
        public void sendTitle(@NonNull org.bukkit.entity.Player player, @Nullable String title, @Nullable String subtitle, @NonNull TagResolver @NonNull ... tagResolvers) {
            player.showTitle(Title.title(
                    title == null ? empty() : MiniMessage.miniMessage().deserialize(title, tagResolvers),
                    subtitle == null ? empty() : MiniMessage.miniMessage().deserialize(subtitle, tagResolvers)
            ));
        }

        @Override
        public void broadcastTitle(@Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @Range(from = 0, to = 72_000) int stay, @Range(from = 0, to = 72_000) int fadeOut, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastAction(player -> sendTitle(player, title, subtitle, fadeIn, stay, fadeOut, tagResolvers));
        }

        @Override
        public void broadcastTitle(@Nullable String title, @Nullable String subtitle, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastAction(player -> sendTitle(player, title, subtitle, tagResolvers));
        }

        @Override
        public void broadcastTitle(@Nullable String title, @Nullable String subtitle, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastTitle(title, subtitle, player -> true, tagResolvers);
        }

        @Override
        public void broadcastTitle(@Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastTitle(title, subtitle, fadeIn, stay, fadeOut, player -> true, tagResolvers);
        }

        @Override
        public void sendPersistentTitle(@NonNull org.bukkit.entity.Player player, @Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @NonNull TagResolver @NonNull ... tagResolvers) {
            sendTitle(player, title, subtitle, fadeIn, 72_000 /* 1h */, 0, tagResolvers);
        }

        @Override
        public void broadcastPersistentTitle(@Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fade, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastAction(player -> sendPersistentTitle(player, title, subtitle, fade, tagResolvers));
        }

        @Override
        public void broadcastPersistentTitle(@Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fade, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastAction(player -> sendPersistentTitle(player, title, subtitle, fade, tagResolvers));
        }

        /**
         * Broadcast a {@link PotionEffect} to all players currently connected to this server.
         *
         * @param potionEffectType The {@link PotionEffectType}.
         * @param duration         The duration (in ticks).
         * @param amplifier        The amplifier.
         * @param playerPredicate  The {@link Predicate} specifying the condition in which the potion effect is broadcast.
         */
        public void broadcastPotionEffect(@NonNull PotionEffectType potionEffectType, int duration, int amplifier, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate) {
            broadcastAction(playerPredicate, player -> player.addPotionEffect(new PotionEffect(potionEffectType, duration, amplifier)));
        }

        /**
         * Broadcasts a {@link PotionEffect} to all players currently connected to this server.
         *
         * @param potionEffectType The {@link PotionEffectType}.
         * @param duration         The duration (in ticks).
         * @param amplifier        The amplifier.
         */
        public void broadcastPotionEffect(@NonNull PotionEffectType potionEffectType, int duration, int amplifier) {
            broadcastPotionEffect(potionEffectType, duration, amplifier, player -> true);
        }

        /**
         * Clears a potion effect for all players currently connected to this server, matching the given {@link PotionEffectType}.
         *
         * @param potionEffectType The {@link PotionEffectType}.
         * @param playerPredicate  The {@link Predicate} specifying the condition in which the potion effect is removed.
         */
        public void broadcastClearPotionEffect(@NonNull PotionEffectType potionEffectType, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate) {
            broadcastAction(playerPredicate, player -> player.removePotionEffect(potionEffectType));
        }

        /**
         * Clears a potion effect for all players currently connected to this server matching the given {@link PotionEffectType}.
         *
         * @param potionEffectType The {@link PotionEffectType}.
         */
        public void broadcastClearPotionEffect(@NonNull PotionEffectType potionEffectType) {
            broadcastClearPotionEffect(potionEffectType, player -> true);
        }

        /**
         * Clears all potion effects for all players currently connected to this server.
         *
         * @param playerPredicate The {@link Predicate} specifying the condition in which all potion effects are removed.
         */
        public void broadcastClearPotionEffects(@NonNull Predicate<org.bukkit.entity.Player> playerPredicate) {
            broadcastAction(playerPredicate, player -> player.getActivePotionEffects().stream()
                    .map(PotionEffect::getType)
                    .forEach(player::removePotionEffect));
        }

        /**
         * Clears all potion effects for all players currently connected to this server.
         */
        public void broadcastClearPotionEffects() {
            broadcastClearPotionEffects(player -> true);
        }

        @Override
        public void sendActionBar(@NonNull org.bukkit.entity.Player player, @NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize(message, tagResolvers));
        }

        @Override
        public void broadcastActionBar(@NonNull String message, @NonNull Predicate<org.bukkit.entity.Player> playerPredicate, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastAction(playerPredicate, player -> sendActionBar(player, message, tagResolvers));
        }

        @Override
        public void broadcastActionBar(@NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers) {
            broadcastActionBar(message, player -> true, tagResolvers);
        }

        /**
         * Teleports the given player to the specified location with the given effect.
         *
         * @param player           The player to teleport.
         * @param location         The location to teleport the player to.
         * @param potionEffectType The potion effect for the teleportation.
         */
        public void teleport(@NonNull org.bukkit.entity.Player player, @NonNull Location location, @NonNull PotionEffectType potionEffectType) {
            player.removePotionEffect(potionEffectType);
            player.addPotionEffect(new PotionEffect(potionEffectType, 2, Integer.MAX_VALUE));
            player.teleport(location);
            player.removePotionEffect(potionEffectType);
        }

        /**
         * Teleports the given player to the specified location with an effect.
         *
         * @param player   The player to teleport.
         * @param location The location to teleport the player to.
         */
        public void teleport(@NonNull org.bukkit.entity.Player player, @NonNull Location location) {
            teleport(player, location, PotionEffectType.SLOW);
        }

        /**
         * Teleports the given player to the specified target entity with an effect.
         *
         * @param player The player to teleport.
         * @param to     The entity to teleport to.
         */
        public void teleport(@NonNull org.bukkit.entity.Player player, @NonNull Entity to) {
            teleport(player, to.getLocation(), PotionEffectType.SLOW);
        }
    }

    /**
     * The bungeecord implementation for every vital util.
     */
    class Bungeecord implements VitalUtils<ProxiedPlayer> {
        @Override
        public void broadcastAction(@NonNull Predicate<ProxiedPlayer> playerPredicate, @NonNull Consumer<ProxiedPlayer> action) {
            ProxyServer.getInstance().getPlayers().stream()
                    .filter(playerPredicate)
                    .forEach(action);
        }

        @Override
        public void sendMessage(@NonNull ProxiedPlayer proxiedPlayer, @NonNull String message, @Nullable TagResolver... tagResolvers) {
            proxiedPlayer.sendMessage(BungeeComponentSerializer.get().serialize(MiniMessage.miniMessage().deserialize(message, tagResolvers == null ? new TagResolver[0] : tagResolvers)));
        }

        @Override
        public void sendTitle(@NonNull ProxiedPlayer player, @Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @Range(from = 0, to = 72_000) int stay, @Range(from = 0, to = 72_000) int fadeOut, @NonNull TagResolver @NonNull ... tagResolvers) {
            player.sendTitle(ProxyServer.getInstance().createTitle()
                    .title(TextComponent.fromLegacy(title == null ? "" : LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(title, tagResolvers))))
                    .subTitle(TextComponent.fromLegacy(subtitle == null ? "" : LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(subtitle, tagResolvers))))
                    .fadeIn(fadeIn)
                    .stay(stay)
                    .fadeOut(fadeOut)
            );
        }

        @Override
        public void sendTitle(@NonNull ProxiedPlayer player, @Nullable String title, @Nullable String subtitle, @NonNull TagResolver @NonNull ... tagResolvers) {
            player.sendTitle(ProxyServer.getInstance().createTitle()
                    .title(TextComponent.fromLegacy(title == null ? "" : LegacyComponentSerializer.legacyAmpersand().serialize(MiniMessage.miniMessage().deserialize(title, tagResolvers))))
                    .subTitle(TextComponent.fromLegacy(subtitle == null ? "" : LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(subtitle, tagResolvers))))
            );
        }

        @Override
        public void sendPersistentTitle(@NonNull ProxiedPlayer player, @Nullable String title, @Nullable String subtitle, @Range(from = 0, to = 72_000) int fadeIn, @NonNull TagResolver @NonNull ... tagResolvers) {
            sendTitle(player, title, subtitle, fadeIn, 72_000 /* 1h */, 0, tagResolvers);
        }

        @Override
        public void sendActionBar(@NonNull ProxiedPlayer proxiedPlayer, @NonNull String message, @NonNull TagResolver @NonNull ... tagResolvers) {
            proxiedPlayer.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(message, tagResolvers))));
        }
    }
}