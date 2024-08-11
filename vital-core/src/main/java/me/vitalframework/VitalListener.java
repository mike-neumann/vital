package me.vitalframework;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A listener component for handling events in the Vital-Framework.
 *
 * @author xRa1ny
 */
public abstract class VitalListener<T> {
    @Getter
    @Autowired
    private T plugin;

    /**
     * Constructor for when using dependency injection
     */
    protected VitalListener() {

    }

    /**
     * Constructor for when not using dependency injection
     */
    protected VitalListener(T plugin) {
        this.plugin = plugin;
    }

    public static abstract class Spigot extends VitalListener<JavaPlugin> implements org.bukkit.event.Listener {
        public Spigot() {
        }

        public Spigot(JavaPlugin plugin) {
            super(plugin);
        }

        @PostConstruct
        public void init() {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }
    }

    public static abstract class Bungeecord extends VitalListener<Plugin> implements net.md_5.bungee.api.plugin.Listener {
        public Bungeecord() {
        }

        public Bungeecord(Plugin plugin) {
            super(plugin);
        }

        @PostConstruct
        public void init() {
            getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), this);
        }
    }
}