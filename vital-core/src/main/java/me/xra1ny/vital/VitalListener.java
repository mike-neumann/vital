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

    public interface Spigot extends org.bukkit.event.Listener {
        @AfterInit
        default void afterInit(JavaPlugin plugin) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public interface Bungeecord extends net.md_5.bungee.api.plugin.Listener {
        @AfterInit
        default void afterInit(Plugin plugin) {
            plugin.getProxy().getPluginManager().registerListener(plugin, this);
        }
    }
}