package me.vitalframework

import jakarta.annotation.PostConstruct
import org.bukkit.Bukkit

/**
 * Abstract base class for registering multi-platform listener implementations.
 */
abstract class VitalListener<T>(
    val plugin: T,
) {
    abstract class Spigot(plugin: SpigotPlugin) : VitalListener<SpigotPlugin>(plugin), SpigotListener {
        @PostConstruct
        fun init() {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }
    }

    abstract class Bungee(plugin: BungeePlugin) : VitalListener<BungeePlugin>(plugin), BungeeListener {
        @PostConstruct
        fun init() {
            plugin.proxy.pluginManager.registerListener(plugin, this)
        }
    }
}