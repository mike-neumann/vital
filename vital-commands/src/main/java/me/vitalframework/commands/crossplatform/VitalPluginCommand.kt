package me.vitalframework.commands.crossplatform

import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter

class VitalPluginCommand private constructor() {
    interface Spigot : CommandExecutor, TabCompleter

    abstract class Bungee(name: String) : Command(name), TabExecutor
}