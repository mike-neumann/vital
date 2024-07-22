package me.xra1ny.vital.configs;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import me.xra1ny.vital.configs.annotation.Property;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Wrapper class to store player data to a config file.
 *
 * @param <T> The player type of this config player.
 * @author xRa1ny
 */
public abstract class ConfigPlayer<T> {
    @Property(String.class)
    public String name;

    @Property(UUID.class)
    public UUID uuid;

    @Nullable
    public abstract T toPlayer();

    public static class Spigot extends ConfigPlayer<Player> {
        @Property(ConfigLocation.class)
        public ConfigLocation location;

        @Property(double.class)
        public double health;

        @Property(double.class)
        public double foodLevel;

        @NonNull
        public static ConfigPlayer.Spigot of(@NonNull Player player) {
            final ConfigPlayer.Spigot configPlayer = new ConfigPlayer.Spigot();

            configPlayer.name = player.getName();
            configPlayer.uuid = player.getUniqueId();
            configPlayer.location = ConfigLocation.of(player.getLocation());
            configPlayer.health = player.getHealth();
            configPlayer.foodLevel = player.getFoodLevel();

            return configPlayer;
        }

        @Override
        @Nullable
        public Player toPlayer() {
            return Bukkit.getPlayer(uuid);
        }
    }

    public static class Bungeecord extends ConfigPlayer<ProxiedPlayer> {
        @NonNull
        public static ConfigPlayer.Bungeecord of(@NonNull ProxiedPlayer player) {
            final ConfigPlayer.Bungeecord configPlayer = new ConfigPlayer.Bungeecord();

            configPlayer.name = player.getName();
            configPlayer.uuid = player.getUniqueId();

            return configPlayer;
        }

        @Override
        @Nullable
        public ProxiedPlayer toPlayer() {
            return ProxyServer.getInstance().getPlayer(uuid);
        }
    }
}