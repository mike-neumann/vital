package me.xra1ny.vital.players;

import lombok.Getter;
import lombok.NonNull;
import me.xra1ny.vital.VitalComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Represents a player as a VitalComponent, providing access to the player's data and functionality.
 *
 * @author xRa1ny
 */
public abstract class VitalPlayer<Player> implements VitalComponent {
    /**
     * The Minecraft player associated with this VitalPlayer.
     */
    @Getter
    @NonNull
    private final Player player;

    /**
     * Creates a new instance of VitalPlayer for the given Minecraft player.
     *
     * @param player The Minecraft player to associate with this VitalPlayer.
     */
    private VitalPlayer(@NonNull Player player) {
        this.player = player;
    }

    /**
     * Gets the unique ID of the associated Minecraft player.
     *
     * @return The unique ID of the player.
     */
    @Override
    @NonNull
    public abstract UUID getUniqueId();

    /**
     * Gets the name of the associated Minecraft player.
     *
     * @return The name of the player.
     */
    @Override
    @NonNull
    public abstract String getName();

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

    public static class Spigot extends VitalPlayer<org.bukkit.entity.Player> {
        public Spigot(@NonNull org.bukkit.entity.Player player) {
            super(player);
        }

        @Override
        public @NonNull UUID getUniqueId() {
            return getPlayer().getUniqueId();
        }

        @Override
        public @NonNull String getName() {
            return getPlayer().getName();
        }
    }

    public static class Bungeecord extends VitalPlayer<ProxiedPlayer> {
        public Bungeecord(@NonNull ProxiedPlayer proxiedPlayer) {
            super(proxiedPlayer);
        }

        @Override
        public @NonNull UUID getUniqueId() {
            return getPlayer().getUniqueId();
        }

        @Override
        public @NonNull String getName() {
            return getPlayer().getName();
        }
    }
}