package me.vitalframework

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("vital-core")
class VitalCoreSubModule : VitalSubModule()
typealias SpigotEventHandler = org.bukkit.event.EventHandler
typealias BungeeEventHandler = net.md_5.bungee.event.EventHandler
typealias SpigotCommandSender = org.bukkit.command.CommandSender
typealias BungeeCommandSender = net.md_5.bungee.api.CommandSender
typealias SpigotPlugin = org.bukkit.plugin.java.JavaPlugin
typealias BungeePlugin = net.md_5.bungee.api.plugin.Plugin
typealias SpigotListener = org.bukkit.event.Listener
typealias BungeeListener = net.md_5.bungee.api.plugin.Listener
typealias SpigotPlayer = org.bukkit.entity.Player
typealias BungeePlayer = net.md_5.bungee.api.connection.ProxiedPlayer
typealias SpigotEventPriority = org.bukkit.event.EventPriority
typealias BungeeEventPriority = net.md_5.bungee.event.EventPriority
typealias SpigotRunnable = org.bukkit.scheduler.BukkitRunnable
typealias BungeeRunnable = Runnable
typealias SpigotTask = org.bukkit.scheduler.BukkitTask
typealias BungeeTask = net.md_5.bungee.api.scheduler.ScheduledTask
typealias SpigotEvent = org.bukkit.event.Event
typealias BungeeEvent = net.md_5.bungee.api.plugin.Event
typealias SpigotCancellable = org.bukkit.event.Cancellable
typealias BungeeCancellable = net.md_5.bungee.api.plugin.Cancellable

fun <T : Any> T.logger() = LoggerFactory.getLogger(this::class.java)!!
