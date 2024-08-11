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
public abstract class VitalPlayerListener<P, T, VP extends VitalPlayer<?>, PM extends VitalRepository<VP>> {
    @Getter
    @Autowired
    private PM playerManager;

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
    public VitalPlayerListener(P plugin, PM playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    public void handlePlayerJoin(@NonNull UUID uniqueId, @NonNull T player) {
        // Retrieve the VitalPlayer associated with the joining player, if it exists.
        final Optional<VP> optionalVitalPlayer = Optional.ofNullable(playerManager.getComponentByUniqueId(uniqueId));

        if (optionalVitalPlayer.isEmpty()) {
            // Create a new VitalPlayer for the joining player.
            try {
                final VP vitalPlayer = vitalPlayerType().getDeclaredConstructor(playerType()).newInstance(player);

                // Register the VitalPlayer with VitalUserManagement.
                playerManager.registerComponent(vitalPlayer);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("error while creating vital player instance %s for %s"
                        .formatted(vitalPlayerType().getSimpleName(), uniqueId));
            }
        }
    }

    public void handlePlayerQuit(@NonNull UUID uniqueId) {
        // Retrieve the VitalPlayer associated with the leaving player.
        final Optional<VP> optionalVitalPlayer = Optional.ofNullable(playerManager.getComponentByUniqueId(uniqueId));

        if (optionalVitalPlayer.isEmpty()) {
            return;
        }

        final VP vitalPlayer = optionalVitalPlayer.get();

        // Unregister the VitalPlayer from VitalUserManagement.
        playerManager.unregisterComponent(vitalPlayer);
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
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("");
            }

            getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        }

        @Override
        protected Class<Player> playerType() {
            return Player.class;
        }

        // should always be executed first.
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
        public final void onPlayerJoin(@NonNull PlayerJoinEvent e) {
            handlePlayerJoin(e.getPlayer().getUniqueId(), e.getPlayer());
        }

        // should always be executed last.
        @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
        public final void onPlayerQuit(@NonNull PlayerQuitEvent e) {
            handlePlayerQuit(e.getPlayer().getUniqueId());
        }
    }

    public static abstract class Bungeecord<VP extends VitalPlayer.Bungeecord, PM extends VitalRepository<VP>> extends VitalPlayerListener<Plugin, ProxiedPlayer, VP, PM> implements net.md_5.bungee.api.plugin.Listener {
        @PostConstruct
        public void init() {
            // disconnect any connected player...
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                player.disconnect();
            }

            getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), this);
        }

        // should always be executed first.
        @net.md_5.bungee.event.EventHandler(priority = net.md_5.bungee.event.EventPriority.LOWEST)
        public void onPostLogin(PostLoginEvent e) {
            handlePlayerJoin(e.getPlayer().getUniqueId(), e.getPlayer());
        }

        // should always be executed last.
        @net.md_5.bungee.event.EventHandler(priority = net.md_5.bungee.event.EventPriority.HIGHEST)
        public void onPlayerDisconnect(PlayerDisconnectEvent e) {
            handlePlayerQuit(e.getPlayer().getUniqueId());
        }

        @Override
        protected Class<ProxiedPlayer> playerType() {
            return ProxiedPlayer.class;
        }
    }
}