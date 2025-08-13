package me.vitalframework

import jakarta.annotation.PostConstruct
import org.bukkit.Bukkit

/**
 * A listener interface representing a framework component that can be registered to handle platform-specific events.
 * This interface is designed to support multiple server environments via platform-agnostic definitions.
 *
 * @param T The type of the plugin associated with the listener.
 */
interface VitalListener<T> {
    val plugin: T

    abstract class Spigot(
        override val plugin: SpigotPlugin,
    ) : VitalListener<SpigotPlugin>,
        SpigotListener {
        @PostConstruct
        fun init() = Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    abstract class Bungee(
        override val plugin: BungeePlugin,
    ) : VitalListener<BungeePlugin>,
        BungeeListener {
        @PostConstruct
        fun init() = plugin.proxy.pluginManager.registerListener(plugin, this)
    }
}
