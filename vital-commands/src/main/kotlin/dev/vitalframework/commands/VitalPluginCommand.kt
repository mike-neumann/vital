package dev.vitalframework.commands

import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.TabCompleter

/**
 * Internal class; used to reduce boilerplate for instantiating commands in [VitalCommand].
 */
object VitalPluginCommand {
    interface Spigot :
        CommandExecutor,
        TabCompleter

    abstract class Bungee(
        name: String,
    ) : Command(name),
        TabExecutor
}
