package me.vitalframework

import jakarta.annotation.PostConstruct
import org.bukkit.Bukkit

interface VitalListener<T> {
    val plugin: T

    abstract class Spigot(override val plugin: SpigotPlugin) : VitalListener<SpigotPlugin>, SpigotListener {
        @PostConstruct
        fun init() {
            Bukkit.getPluginManager().registerEvents(this, plugin)
        }
    }

    abstract class Bungee(override val plugin: BungeePlugin) : VitalListener<BungeePlugin>, BungeeListener {
        @PostConstruct
        fun init() {
            plugin.proxy.pluginManager.registerListener(plugin, this)
        }
    }
}