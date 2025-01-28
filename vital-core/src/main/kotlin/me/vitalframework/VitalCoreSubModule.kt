package me.vitalframework

import net.md_5.bungee.api.scheduler.ScheduledTask
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("vital-core")
class VitalCoreSubModule : VitalSubModule()

typealias SpigotEventHandler = EventHandler
typealias BungeeEventHandler = net.md_5.bungee.event.EventHandler
typealias SpigotCommandSender = CommandSender
typealias BungeeCommandSender = net.md_5.bungee.api.CommandSender
typealias SpigotPlugin = JavaPlugin
typealias BungeePlugin = net.md_5.bungee.api.plugin.Plugin
typealias SpigotListener = Listener
typealias BungeeListener = net.md_5.bungee.api.plugin.Listener
typealias SpigotPlayer = org.bukkit.entity.Player
typealias BungeePlayer = net.md_5.bungee.api.connection.ProxiedPlayer
typealias SpigotEventPriority = org.bukkit.event.EventPriority
typealias BungeeEventPriority = net.md_5.bungee.event.EventPriority
typealias SpigotRunnable = BukkitRunnable
typealias BungeeRunnable = Runnable
typealias SpigotTask = BukkitTask
typealias BungeeTask = ScheduledTask

fun <T : Any> T.logger(): Logger = LoggerFactory.getLogger(this::class.java)