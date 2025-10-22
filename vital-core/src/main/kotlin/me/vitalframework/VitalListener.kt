package me.vitalframework

import org.bukkit.Bukkit
import org.springframework.beans.factory.InitializingBean

/**
 * A listener interface representing a framework component that can be registered to handle platform-specific events.
 * This interface is designed to support multiple server environments via platform-agnostic definitions.
 *
 * @param T The type of the plugin associated with the listener.
 */
interface VitalListener<T> : InitializingBean {
    val plugin: T

    abstract class Spigot(
        override val plugin: SpigotPlugin,
    ) : VitalListener<SpigotPlugin>,
        SpigotListener {
        final override fun afterPropertiesSet() {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }
    }

    abstract class Bungee(
        override val plugin: BungeePlugin,
    ) : VitalListener<BungeePlugin>,
        BungeeListener {
        override fun afterPropertiesSet() {
            plugin.proxy.pluginManager.registerListener(plugin, this)
        }
    }
}
