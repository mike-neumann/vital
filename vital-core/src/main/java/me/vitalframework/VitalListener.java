package me.vitalframework;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A listener component for handling events in the Vital-Framework.
 *
 * @author xRa1ny
 */
@Getter
@RequiredArgsConstructor
public abstract class VitalListener<T> {
    @NonNull
    private final T plugin;

    public static abstract class Spigot extends VitalListener<JavaPlugin> implements org.bukkit.event.Listener {
        public Spigot(@NonNull JavaPlugin plugin) {
            super(plugin);
        }

        @PostConstruct
        public void init() {
            Bukkit.getPluginManager().registerEvents(this, getPlugin());
        }
    }

    public static abstract class Bungeecord extends VitalListener<Plugin> implements net.md_5.bungee.api.plugin.Listener {
        public Bungeecord(@NonNull Plugin plugin) {
            super(plugin);
        }

        @PostConstruct
        public void init() {
            getPlugin().getProxy().getPluginManager().registerListener(getPlugin(), this);
        }
    }
}