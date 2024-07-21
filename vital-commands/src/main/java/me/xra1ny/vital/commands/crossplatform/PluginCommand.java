package me.xra1ny.vital.commands.crossplatform;

import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.springframework.stereotype.Component;

/**
 * Convenience class holding subclasses for multiple vital instance command implementations.
 *
 * @author xRa1ny
 */
public class PluginCommand {
    private PluginCommand() {
        // may not be instantiated.
    }

    public interface Spigot extends CommandExecutor, TabCompleter {

    }

    public static abstract class Bungeecord extends Command implements TabExecutor {
        public Bungeecord(String name) {
            super(name);
        }
    }
}