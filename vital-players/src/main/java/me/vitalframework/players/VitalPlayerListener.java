package me.vitalframework.players;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.vitalframework.VitalRepository;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

/**
 * A listener class that manages VitalPlayer instances when players join and leave the server.
 *
 * @author xRa1ny
 */
@RequiredArgsConstructor
@Getter
public abstract class VitalPlayerListener<P, T, VP extends VitalPlayer<?>, PR extends VitalRepository<VP>> {
    @NonNull
    private final P plugin;

    @NonNull
    private final PR playerRepository;

    public void handlePlayerJoin(@NonNull UUID uniqueId, @NonNull T player) {
        // Retrieve the VitalPlayer associated with the joining player, if it exists.
        final var optionalVitalPlayer = Optional.ofNullable(playerRepository.getComponentByUniqueId(uniqueId));

        if (optionalVitalPlayer.isEmpty()) {
            // Create a new VitalPlayer for the joining player.
            try {
                final var vitalPlayer = vitalPlayerType().getDeclaredConstructor(playerType()).newInstance(player);

                // Register the VitalPlayer with VitalUserManagement.
                playerRepository.registerComponent(vitalPlayer);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("error while creating vital player instance %s for %s"
                        .formatted(vitalPlayerType().getSimpleName(), uniqueId));
            }
        }
    }

    public void handlePlayerQuit(@NonNull UUID uniqueId) {
        // Retrieve the VitalPlayer associated with the leaving player.
        final var optionalVitalPlayer = Optional.ofNullable(playerRepository.getComponentByUniqueId(uniqueId));

        if (optionalVitalPlayer.isEmpty()) {
            return;
        }

        final var vitalPlayer = optionalVitalPlayer.get();

        // Unregister the VitalPlayer from VitalUserManagement.
        playerRepository.unregisterComponent(vitalPlayer);
    }

    /**
     * Defines the type this {@link VitalPlayerListener} manages.
     *
     * @return The type of the managed {@link VitalPlayer}.
     */
    @NonNull
    protected abstract Class<VP> vitalPlayerType();

    /**
     * Defines the player type every VitalPlayer extending class takes as an argument for construction.
     *
     * @return The player type for injection.
     */
    @NonNull
    protected abstract Class<T> playerType();

    public static abstract class Spigot<VP extends VitalPlayer.Spigot, PM extends VitalRepository<VP>> extends VitalPlayerListener<JavaPlugin, Player, VP, PM> implements org.bukkit.event.Listener {
        public Spigot(@NonNull JavaPlugin plugin, @NonNull PM playerRepository) {
            super(plugin, playerRepository);
        }

        @PostConstruct
        public void init() {
            // disconnect any connected player...
            for (var player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("");
            }

            getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        }

        @Override
        protected @NonNull Class<Player> playerType() {
            return Player.class;
        }

        // should always be executed first.
        @org.bukkit.event.EventHandler(priority = EventPriority.LOW)
        public final void onPlayerJoin(@NonNull PlayerJoinEvent e) {
            handlePlayerJoin(e.getPlayer().getUniqueId(), e.getPlayer());
        }

        // should always be executed last.
        @org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
        public final void onPlayerQuit(@NonNull PlayerQuitEvent e) {
            handlePlayerQuit(e.getPlayer().getUniqueId());
        }
    }

    public static abstract class Bungeecord<VP extends VitalPlayer.Bungeecord, PM extends VitalRepository<VP>> extends VitalPlayerListener<Plugin, ProxiedPlayer, VP, PM> implements net.md_5.bungee.api.plugin.Listener {
        public Bungeecord(@NonNull Plugin plugin, @NonNull PM playerRepository) {
            super(plugin, playerRepository);
        }

        @PostConstruct
        public void init() {
            // disconnect any connected player...
            for (var player : ProxyServer.getInstance().getPlayers()) {
                player.disconnect();
            }

            getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), this);
        }

        // should always be executed first.
        @net.md_5.bungee.event.EventHandler(priority = net.md_5.bungee.event.EventPriority.LOW)
        public void onPostLogin(PostLoginEvent e) {
            handlePlayerJoin(e.getPlayer().getUniqueId(), e.getPlayer());
        }

        // should always be executed last.
        @net.md_5.bungee.event.EventHandler(priority = net.md_5.bungee.event.EventPriority.HIGH)
        public void onPlayerDisconnect(PlayerDisconnectEvent e) {
            handlePlayerQuit(e.getPlayer().getUniqueId());
        }

        @Override
        protected @NonNull Class<ProxiedPlayer> playerType() {
            return ProxiedPlayer.class;
        }
    }
}