package me.vitalframework.players;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.VitalComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a player as a VitalComponent, providing access to the player's data and functionality.
 *
 * @author xRa1ny
 */
public abstract class VitalPlayer<T> implements VitalComponent {
    /**
     * The Minecraft player associated with this VitalPlayer.
     */
    @Getter
    private final T player;

    /**
     * Creates a new instance of VitalPlayer for the given Minecraft player.
     *
     * @param player The Minecraft player to associate with this VitalPlayer.
     */
    private VitalPlayer(@NonNull T player) {
        this.player = player;
    }

    /**
     * Called when this VitalComponent is registered.
     */
    @Override
    public void onRegistered() {

    }

    /**
     * Called when this VitalComponent is unregistered.
     */
    @Override
    public void onUnregistered() {

    }

    public static class Spigot extends VitalPlayer<Player> {
        public Spigot(@NonNull Player player) {
            super(player);
        }

        @Override
        @NonNull
        public final UUID getUniqueId() {
            return getPlayer().getUniqueId();
        }

        @Override
        @NonNull
        public final String getName() {
            return getPlayer().getName();
        }
    }

    /**
     * The bungeecord implementation for vital player.
     */
    public static class Bungeecord extends VitalPlayer<ProxiedPlayer> {
        public Bungeecord(@NonNull ProxiedPlayer proxiedPlayer) {
            super(proxiedPlayer);
        }

        @Override
        @NonNull
        public final UUID getUniqueId() {
            return getPlayer().getUniqueId();
        }

        @Override
        @NonNull
        public final String getName() {
            return getPlayer().getName();
        }
    }
}