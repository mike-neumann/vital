package me.vitalframework.configs;

import lombok.NonNull;
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
public abstract class VitalConfigPlayer<T> {
    @VitalConfig.Property(String.class)
    public String name;

    @VitalConfig.Property(UUID.class)
    public UUID uuid;


    public abstract T toPlayer();

    public static class Spigot extends VitalConfigPlayer<Player> {
        @VitalConfig.Property(VitalConfigLocation.class)
        public VitalConfigLocation location;

        @VitalConfig.Property(double.class)
        public double health;

        @VitalConfig.Property(double.class)
        public double foodLevel;

        @NonNull
        public static VitalConfigPlayer.Spigot of(@NonNull Player player) {
            final var configPlayer = new VitalConfigPlayer.Spigot();

            configPlayer.name = player.getName();
            configPlayer.uuid = player.getUniqueId();
            configPlayer.location = VitalConfigLocation.of(player.getLocation());
            configPlayer.health = player.getHealth();
            configPlayer.foodLevel = player.getFoodLevel();

            return configPlayer;
        }

        @Override

        public Player toPlayer() {
            return Bukkit.getPlayer(uuid);
        }
    }

    public static class Bungeecord extends VitalConfigPlayer<ProxiedPlayer> {
        @NonNull
        public static VitalConfigPlayer.Bungeecord of(@NonNull ProxiedPlayer player) {
            final var configPlayer = new VitalConfigPlayer.Bungeecord();

            configPlayer.name = player.getName();
            configPlayer.uuid = player.getUniqueId();

            return configPlayer;
        }

        @Override

        public ProxiedPlayer toPlayer() {
            return ProxyServer.getInstance().getPlayer(uuid);
        }
    }
}