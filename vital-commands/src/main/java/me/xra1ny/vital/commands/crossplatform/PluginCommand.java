package me.xra1ny.vital.commands.crossplatform;

import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

/**
 * Convenience class holding subclasses for multiple vital instance command implementations.
 *
 * @author xRa1ny
 */
public class PluginCommand {
    private PluginCommand() {
        // may not be instantiated.
    }

    /**
     * Convenient implementation for spigot commands.
     */
    public interface Spigot extends CommandExecutor, TabCompleter {

    }

    /**
     * Convenient implementation for bungeecord commands.
     */
    public static abstract class Bungeecord extends Command implements TabExecutor {
        /**
         * Constructs a new bungeecord implementation for convenient plugin command creation.
         *
         * @param name The name of the command.
         */
        public Bungeecord(String name) {
            super(name);
        }
    }
}