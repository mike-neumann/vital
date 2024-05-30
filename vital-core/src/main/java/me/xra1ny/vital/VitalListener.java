package me.xra1ny.vital;

import me.xra1ny.essentia.inject.annotation.AfterInit;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A listener component for handling events in the Vital-Framework.
 *
 * @author xRa1ny
 */
public class VitalListener {
    private VitalListener() {

    }

    /**
     * The spigot implementation for vital listeners.
     */
    public interface Spigot extends org.bukkit.event.Listener {
        /**
         * Registers this listener.
         *
         * @param plugin The spigot plugin impl.
         */
        @AfterInit
        default void afterInit(JavaPlugin plugin) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    /**
     * The bungeecord implementation for vital listeners.
     */
    public interface Bungeecord extends net.md_5.bungee.api.plugin.Listener {
        /**
         * Registers this listener.
         *
         * @param plugin The bungeecord impl.
         */
        @AfterInit
        default void afterInit(Plugin plugin) {
            plugin.getProxy().getPluginManager().registerListener(plugin, this);
        }
    }
}