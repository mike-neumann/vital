package me.vitalframework.players;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

/**
 * A listener class that manages VitalPlayer instances when players join and leave the server.
 *
 * @author xRa1ny
 */
public abstract class VitalPlayerListener<P, T, VP extends VitalPlayer<?>, PR extends VitalRepository<VP>> {
    @Getter
    @Autowired
    private PR playerRepository;

    @Getter
    @Autowired
    private P plugin;

    /**
     * Constructor for when using dependency injection
     */
    public VitalPlayerListener() {

    }

    /**
     * Constructor for when not using dependency injection
     */
    public VitalPlayerListener(P plugin, PR playerRepository) {
        this.plugin = plugin;
        this.playerRepository = playerRepository;
    }

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
    protected abstract Class<VP> vitalPlayerType();

    /**
     * Defines the player type every VitalPlayer extending class takes as an argument for construction.
     *
     * @return The player type for injection.
     */
    protected abstract Class<T> playerType();

    public static abstract class Spigot<VP extends VitalPlayer.Spigot, PM extends VitalRepository<VP>> extends VitalPlayerListener<JavaPlugin, Player, VP, PM> implements org.bukkit.event.Listener {
        @PostConstruct
        public void init() {
            // disconnect any connected player...
            for (var player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("");
            }

            getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        }

        @Override
        protected Class<Player> playerType() {
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
        protected Class<ProxiedPlayer> playerType() {
            return ProxiedPlayer.class;
        }
    }
}