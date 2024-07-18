package me.xra1ny.vital;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A listener component for handling events in the Vital-Framework.
 *
 * @author xRa1ny
 */
public abstract class VitalListener<Plugin> {
    @Autowired
    @Getter
    private Plugin plugin;

    private VitalListener() {

    }

    /**
     * The spigot implementation for vital listeners.
     */
    public static abstract class Spigot extends VitalListener<JavaPlugin> implements org.bukkit.event.Listener {
        @PostConstruct
        public void init() {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }
    }

    /**
     * The bungeecord implementation for vital listeners.
     */
    public static abstract class Bungeecord extends VitalListener<net.md_5.bungee.api.plugin.Plugin> implements net.md_5.bungee.api.plugin.Listener {
        @PostConstruct
        public void init() {
            getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), this);
        }
    }
}