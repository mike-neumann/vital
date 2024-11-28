package me.vitalframework.players;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.vitalframework.VitalComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a player as a VitalComponent, providing access to the player's data and functionality.
 *
 * @author xRa1ny
 */
@RequiredArgsConstructor
@Getter
public abstract class VitalPlayer<T> implements VitalComponent {
    /**
     * The Minecraft player associated with this VitalPlayer.
     */
    @NonNull
    private final T player;

    @Override
    public void onRegistered() {

    }

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