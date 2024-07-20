package me.xra1ny.vital.players;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import me.xra1ny.vital.VitalComponentManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
public abstract class VitalPlayerListener<Plugin, Player, VPlayer extends VitalPlayer<?>, PlayerManager extends VitalComponentManager<VPlayer>> {
    @Getter
    private final PlayerManager playerManager;

    // can be ignored since subclasses is always a component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Getter
    private Plugin plugin;

    protected VitalPlayerListener(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    /**
     * Handles the event when a player joins the server.
     *
     * @param uniqueId The uniqueId of the player.
     * @param player   The player joining itself.
     */
    public void handlePlayerJoin(@NonNull UUID uniqueId, @NonNull Player player) {
        // Retrieve the VitalPlayer associated with the joining player, if it exists.
        final Optional<VPlayer> optionalVitalPlayer = Optional.ofNullable(playerManager.getComponentByUniqueId(uniqueId));

        if (optionalVitalPlayer.isEmpty()) {
            // Create a new VitalPlayer for the joining player.
            try {
                final VPlayer vitalPlayer = vitalPlayerType().getDeclaredConstructor(playerType()).newInstance(player);

                // Register the VitalPlayer with VitalUserManagement.
                playerManager.registerComponent(vitalPlayer);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("error while creating vital player instance %s for %s"
                        .formatted(vitalPlayerType().getSimpleName(), uniqueId));
            }
        }
    }

    /**
     * Handles the event when a player leaves the server.
     *
     * @param uniqueId The uniqueId of the player.
     */
    public void handlePlayerQuit(@NonNull UUID uniqueId) {
        // Retrieve the VitalPlayer associated with the leaving player.
        final Optional<VPlayer> optionalVitalPlayer = Optional.ofNullable(playerManager.getComponentByUniqueId(uniqueId));

        if (optionalVitalPlayer.isEmpty()) {
            return;
        }

        final VPlayer vitalPlayer = optionalVitalPlayer.get();

        // Unregister the VitalPlayer from VitalUserManagement.
        playerManager.unregisterComponent(vitalPlayer);
    }

    /**
     * Defines the type this {@link VitalPlayerListener} manages.
     *
     * @return The type of the managed {@link VitalPlayer}.
     */
    protected abstract Class<VPlayer> vitalPlayerType();

    /**
     * Defines the player type every VitalPlayer extending class takes as an argument for construction.
     *
     * @return The player type for injection.
     */
    protected abstract Class<Player> playerType();

    public abstract static class Spigot<VPlayer extends VitalPlayer.Spigot, PlayerManager extends VitalComponentManager<VPlayer>> extends VitalPlayerListener<JavaPlugin, org.bukkit.entity.Player, VPlayer, PlayerManager> implements Listener {
        protected Spigot(PlayerManager vital) {
            super(vital);
        }

        @PostConstruct
        public void init() {
            // disconnect any connected player...
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("");
            }

            getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        }

        @Override
        protected Class<org.bukkit.entity.Player> playerType() {
            return org.bukkit.entity.Player.class;
        }

        // should always be executed first.
        @EventHandler(priority = EventPriority.LOWEST)
        public final void onPlayerJoin(@NonNull PlayerJoinEvent e) {
            handlePlayerJoin(e.getPlayer().getUniqueId(), e.getPlayer());
        }

        // should always be executed last.
        @EventHandler(priority = EventPriority.HIGHEST)
        public final void onPlayerQuit(@NonNull PlayerQuitEvent e) {
            handlePlayerQuit(e.getPlayer().getUniqueId());
        }
    }

    public static abstract class Bungeecord<VPlayer extends VitalPlayer.Bungeecord, PlayerManager extends VitalComponentManager<VPlayer>> extends VitalPlayerListener<net.md_5.bungee.api.plugin.Plugin, ProxiedPlayer, VPlayer, PlayerManager> implements net.md_5.bungee.api.plugin.Listener {
        protected Bungeecord(PlayerManager vital) {
            super(vital);
        }

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