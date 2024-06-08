package me.xra1ny.vital.commands;

import lombok.Getter;
import lombok.NonNull;
import me.xra1ny.essentia.inject.annotation.AfterInit;
import me.xra1ny.vital.AnnotatedVitalComponent;
import me.xra1ny.vital.commands.annotation.VitalCommandArg;
import me.xra1ny.vital.commands.annotation.VitalCommandArgHandler;
import me.xra1ny.vital.commands.annotation.VitalCommandInfo;
import me.xra1ny.vital.commands.crossplatform.PluginCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base class for custom Minecraft commands using the Vital framework.
 * Provides functionality for command execution, tab completion, and argument handling.
 *
 * @param <CommandSender> The command sender type of this command.
 * @author xRa1ny
 */
public abstract class VitalCommand<CommandSender> implements AnnotatedVitalComponent<VitalCommandInfo> {
    private static final Logger log = LoggerFactory.getLogger(VitalCommand.class);
    private final Class<CommandSender> commandSenderClass;

    /**
     * The name of the command.
     */
    @Getter
    @NonNull
    private final String name;

    /**
     * The required permission to execute this command.
     */
    @Getter
    @NonNull
    private final String permission;

    /**
     * Flag indicating if this command requires a player sender.
     */
    @Getter
    private final boolean requiresPlayer;

    /**
     * Array of VitalCommandArgs describing command arguments.
     */
    @Getter
    @NonNull
    private final Map<Pattern, VitalCommandArg> vitalCommandArgs;

    /**
     * Constructor for VitalCommand.
     */
    private VitalCommand(@NonNull Class<CommandSender> commandSenderClass) {
        final VitalCommandInfo vitalCommandInfo = getRequiredAnnotation();

        this.commandSenderClass = commandSenderClass;
        name = vitalCommandInfo.value();
        permission = vitalCommandInfo.permission();
        requiresPlayer = vitalCommandInfo.requiresPlayer();
        vitalCommandArgs = Arrays.stream(vitalCommandInfo.args())
                // map spaces
                // map to var-args
                // then map normal args remaining if the first conversion had no effect
                // map to pattern type
                .map(arg -> Map.entry(
                        Pattern.compile(
                                arg.value().replaceAll(" ", "[ ]")
                                        .replaceAll("%.+%[*]", "(.+)")
                                        .replaceAll("%.+%", "(\\\\S+)")
                        ),
                        arg
                ))
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onUnregistered() {

    }

    @Override
    public Class<VitalCommandInfo> requiredAnnotationType() {
        return VitalCommandInfo.class;
    }

    /**
     * If this generic sender is of type player.
     *
     * @param commandSender The sender.
     * @return True if the sender is a player; false otherwise.
     */
    public abstract boolean isPlayer(@NonNull CommandSender commandSender);

    /**
     * Checks if the given sender has the required permissions to run this c command.
     *
     * @param commandSender The sender.
     * @param permission    The permission to check for.
     * @return True if the sender is permitted; false otherwise.
     */
    public abstract boolean hasPermission(@NonNull CommandSender commandSender, @NonNull String permission);

    /**
     * Gets all player names that are currently connected.
     *
     * @return A list of all player names currently connected.
     */
    public abstract List<String> getAllPlayerNames();

    /**
     * Execute this command with any set specifications.
     *
     * @param sender Who sent the command.
     * @param args   Any args used during command execution.
     * @return Command success state.
     */
    public final boolean execute(@NonNull CommandSender sender, @NonNull String[] args) {
        // Check if the command requires a player sender.
        if (requiresPlayer) {
            // Check if the sender is not a Player.
            if (!isPlayer(sender)) {
                // Execute the onCommandRequiresPlayer method and return true.
                onCommandRequiresPlayer(sender, String.join(" ", args), null);

                return true;
            }
        }

        // Check if a permission is required and if the sender has it.
        if (!permission.isBlank() && !hasPermission(sender, permission)) {
            // Execute the onCommandRequiresPermission method and return true.
            onCommandRequiresPermission(sender, String.join(" ", args), null);

            return true;
        }

        try {
            // find executing command arg
            final VitalCommandArg executingArg = getExecutingArg(String.join(" ", args));
            VitalCommandReturnState commandReturnState;

            log.info(vitalCommandArgs.toString());

            try {
                if (executingArg == null) {
                    commandReturnState = executeBaseCommand(sender);
                } else {
                    // extract user values from command
                    final List<String> values = new ArrayList<>();

                    for (String commandArg : executingArg.value().split(" ")) {
                        for (String userArg : args) {
                            if (!userArg.equalsIgnoreCase(commandArg)) {
                                // we have a custom arg
                                values.add(userArg);
                            }
                        }
                    }

                    commandReturnState = executeCommandArgHandlerMethod(sender, executingArg, values.toArray(String[]::new));
                }
            } catch (Exception e) {
                commandReturnState = VitalCommandReturnState.ERROR;
            }

            final String joinedArgs = String.join(" ", args);

            // Handle the command return state.
            switch (commandReturnState) {
                case ERROR -> onCommandError(sender, joinedArgs, executingArg);
                case INTERNAL_ERROR -> onCommandInternalError(sender, joinedArgs, executingArg);
                case INVALID_ARGS -> onCommandInvalidArgs(sender, joinedArgs);
                case NO_PERMISSION -> onCommandRequiresPermission(sender, joinedArgs, executingArg);
            }

            return true;
        } catch (Exception e) {
            log.info("Exception while executing command", e);
        }

        return true;
    }

    @Nullable
    public final VitalCommandArg getExecutingArg(@NonNull String arg) {
        return vitalCommandArgs.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(arg).matches())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * called when this command is executed with only the base command (/commandname)
     *
     * @param sender the sender
     * @return the status of this command execution
     */
    @NonNull
    protected VitalCommandReturnState executeBaseCommand(@NonNull CommandSender sender) {
        return VitalCommandReturnState.INVALID_ARGS;
    }

    @NonNull
    private VitalCommandReturnState executeCommandArgHandlerMethod(@NonNull CommandSender sender, @NonNull VitalCommandArg commandArg, @NonNull String @NonNull [] values) throws InvocationTargetException, IllegalAccessException {
        // Initialize a variable to hold the handler method.
        Method commandArgHandlerMethod = null;

        // Iterate through the methods of the current class.
        for (Method method : getClass().getDeclaredMethods()) {
            log.info("method: {}", method.getName());

            // Retrieve the `@VitalCommandArgHandler` annotation for the method.
            final VitalCommandArgHandler commandArgHandler = method.getDeclaredAnnotation(VitalCommandArgHandler.class);

            // Check if the method does not have the annotation or the annotation value does not match the commandArg value.
            if (commandArgHandler == null || !List.of(commandArgHandler.value()).contains(commandArg.value())) {
                continue; // Skip this method if the condition is not met.
            }

            // If the method's return type does not match `VitalCommandReturnState`, cancel operation.
            if (!VitalCommandReturnState.class.isAssignableFrom(method.getReturnType())) {
                continue;
            }

            // Set the commandArgHandlerMethod to the matching method.
            commandArgHandlerMethod = method;

            break; // Exit the loop after finding the matching method.
        }

        // Check if no handler method was found.
        if (commandArgHandlerMethod == null) {
            return VitalCommandReturnState.INVALID_ARGS; // Return INVALID_ARGS if no handler method is found.
        }

        // If the handler method was found, dynamically inject each parameter supported for its implementation...
        final List<Object> injectedParameters = new ArrayList<>();

        for (Parameter parameter : commandArgHandlerMethod.getParameters()) {
            // If the parameters managed type is of instance of `CommandSender`, inject either `CommandSender` or `Player`
            if (commandSenderClass.isAssignableFrom(parameter.getType())) {
                injectedParameters.add(sender);
            } else if (VitalCommandArg.class.isAssignableFrom(parameter.getType())) {
                // inject `VitalCommandArg`
                injectedParameters.add(commandArg);
            } else if (String[].class.isAssignableFrom(parameter.getType())) {
                // if parameters managed type is an instance of `String[]` inject the values of this command execution.
                injectedParameters.add(values);
            }

            // If the above set conditions are not met, do not inject anything as the type is not supported by Vital.
        }

        // invoke the handler method with the dynamically fetched args...
        return (VitalCommandReturnState) commandArgHandlerMethod.invoke(this, injectedParameters.toArray());
    }

    /**
     * Called upon requesting any tab-completion content.
     *
     * @param sender The {@link CommandSender} that sent the command.
     * @param args   The arguments used in chat.
     * @return A {@link List} of strings to show to the player as tab-completion suggestions.
     */
    @NonNull
    protected List<String> onCommandTabComplete(@NonNull CommandSender sender, @NonNull String args) {
        return List.of();
    }

    /**
     * Handles the tab complete action on any command sender.
     *
     * @param sender The sender.
     * @param args   Any args used during tab completion.
     * @return A list of all supported tab completions.
     */
    @NonNull
    protected final List<String> handleTabComplete(@NonNull CommandSender sender, @NonNull String @NonNull [] args) {
        // Initialize a list to store tab-completed suggestions.
        final List<String> tabCompleted = new ArrayList<>();

        // Loop through the specified command arguments for this command.
        for (VitalCommandArg arg : vitalCommandArgs.values()) {
            // Split the value of the command argument into individual parts.
            final String[] originalArgs = arg.value().split(" ");
            // Clone the originalArgs to avoid modification.
            final String[] editedArgs = originalArgs.clone();

            // Check if the originalArgs length is greater than or equal to the provided args length
            // or if the last element of originalArgs ends with "%*".
            if (originalArgs.length >= args.length || originalArgs[originalArgs.length - 1].endsWith("%*")) {
                // Loop through the provided args.
                for (int i = 0; i < args.length; i++) {
                    // Determine the original argument at the current index.
                    final String originalArg = i >= originalArgs.length ? originalArgs[originalArgs.length - 1] : originalArgs[i];

                    // Check if the original argument does not start with "%" and does not end with "%" or "%*".
                    if (!originalArg.startsWith("%") && !(originalArg.endsWith("%") || originalArg.endsWith("%*"))) {
                        continue; // Skip this iteration if the condition is not met.
                    }

                    // Replace the edited argument at the corresponding index with the provided argument.
                    editedArgs[i >= editedArgs.length ? editedArgs.length - 1 : i] = args[i];
                }

                // Determine the final argument from originalArgs and args.
                final String finalArg = originalArgs[args.length - 1 >= originalArgs.length ? originalArgs.length - 1 : args.length - 1];

                // Check if the joined editedArgs start with the joined provided args.
                if (!String.join(" ", editedArgs).startsWith(String.join(" ", args))) {
                    continue; // Skip this iteration if the condition is not met.
                }

                // Check if the final argument starts with "%" and ends with "%*".
                if (finalArg.startsWith("%") && finalArg.endsWith("%*")) {
                    // Add the final argument with "%" and "%*" removed to the tabCompleted list.
                    tabCompleted.add(finalArg.replace("%", "").replace("%*", ""));

                    break; // Exit the loop.
                }

                if (finalArg.equalsIgnoreCase(VitalCommandArg.MATERIAL)) {
                    for (Material material : Material.values()) {
                        tabCompleted.add(material.name());
                    }
                } else if (finalArg.equalsIgnoreCase(VitalCommandArg.PLAYER)) {
                    // Loop through online players.
                    for (String playerName : getAllPlayerNames()) {
                        // Check if the player name is already in the tabCompleted list.
                        if (tabCompleted.contains(playerName)) {
                            continue; // Skip this iteration if the condition is met.
                        }

                        // Add the player name to the tabCompleted list.
                        tabCompleted.add(playerName);
                    }
                } else if (finalArg.startsWith(VitalCommandArg.NUMBER)) {
                    // Add "0" to the tabCompleted list.
                    tabCompleted.add("0");
                } else if (finalArg.startsWith(VitalCommandArg.BOOLEAN)) {
                    // Add "true" and "false" to the tabCompleted list.
                    tabCompleted.add("true");
                    tabCompleted.add("false");
                } else if (finalArg.startsWith("%") && (finalArg.endsWith("%") || finalArg.endsWith("%*"))) {
                    // Add the final argument with "%" and "%*" removed to the tabCompleted list.
                    tabCompleted.add(finalArg.replace("%", "").replace("%*", ""));
                } else {
                    // Add the final argument to the tabCompleted list.
                    tabCompleted.add(finalArg);
                }
            }
        }

        final String formattedArgs = String.join(" ", Stream.of(args)
                .map(arg -> "?")
                .toList());
        final List<String> commandTabCompleted = onCommandTabComplete(sender, formattedArgs);

        // when our OWN implementation is not empty, clear all of Vital's defaults.
        if (!commandTabCompleted.isEmpty()) {
            tabCompleted.clear();
        }

        // finally add further tab-completed suggestions implemented by the developer.
        tabCompleted.addAll(commandTabCompleted);

        return tabCompleted; // Return the list of tab-completed suggestions.
    }

    /**
     * Called when this VitalCommand has been executed using invalid Arguments
     *
     * @param sender The CommandSender
     * @param args   The args used during command execution
     */
    protected void onCommandInvalidArgs(@NonNull CommandSender sender, @NonNull String args) {

    }

    /**
     * Called when this VitalCommand has been executed and an internal Error has occurred
     *
     * @param sender The CommandSender
     * @param args   The raw args used during command execution
     * @param arg    The actual command arg object being used during command execution
     */
    protected void onCommandInternalError(@NonNull CommandSender sender, @NonNull String args, @Nullable VitalCommandArg arg) {

    }

    /**
     * Called when this VitalCommand has been executed and an Error has occurred
     *
     * @param sender The CommandSender
     * @param args   The raw args used during command execution
     * @param arg    The actual arg object being used during command execution
     */
    protected void onCommandError(@NonNull CommandSender sender, @NonNull String args, @Nullable VitalCommandArg arg) {

    }

    /**
     * Called when this VitalCommand has been executed without needed Permissions
     *
     * @param sender The CommandSender
     * @param args   The raw args being used during command execution
     * @param arg    The actual arg being used during command execution
     */
    protected void onCommandRequiresPermission(@NonNull CommandSender sender, @NonNull String args, @Nullable VitalCommandArg arg) {

    }

    /**
     * Called when this VitalCommand has been executed as a non Player Object while requiring a Player to be executed
     *
     * @param sender The CommandSender
     * @param args   The raw args used during command execution
     * @param arg    The actual arg object used during command execution
     */
    protected void onCommandRequiresPlayer(@NonNull CommandSender sender, @NonNull String args, @Nullable VitalCommandArg arg) {

    }

    /**
     * The spigot implementation for vital commands.
     */
    public static class Spigot extends VitalCommand<org.bukkit.command.CommandSender> implements PluginCommand.Spigot {
        /**
         * Constructs a new spigot vital command with the given info annotation provided by the implementing subclass.
         */
        public Spigot() {
            super(org.bukkit.command.CommandSender.class);
        }

        /**
         * Registers this command.
         *
         * @param plugin The spigot plugin impl.
         */
        @AfterInit
        public final void afterInit(JavaPlugin plugin) {
            plugin.getCommand(getName()).setExecutor(this);
        }

        @Override
        public final boolean onCommand(@NotNull org.bukkit.command.CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return execute(sender, args);
        }

        @Override
        public final List<String> onTabComplete(org.bukkit.command.@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return handleTabComplete(sender, args);
        }

        @Override
        public final boolean isPlayer(@NonNull org.bukkit.command.CommandSender sender) {
            return sender instanceof Player;
        }

        @Override
        public final boolean hasPermission(@NonNull org.bukkit.command.CommandSender sender, @NonNull String permission) {
            return sender.hasPermission(permission);
        }

        @Override
        public List<String> getAllPlayerNames() {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }
    }

    /**
     * The bungeecord implementation for vital commands.
     */
    public static class Bungeecord extends VitalCommand<net.md_5.bungee.api.CommandSender> {
        private final PluginCommand.Bungeecord command;

        /**
         * Constructs a new bungeecord vital command with the given info annotation provided by the implementing subclass.
         */
        public Bungeecord() {
            super(net.md_5.bungee.api.CommandSender.class);

            // wrap a custom bungeecord command class, since in bungeecord, command classes MUST BE EXTENDED FROM.
            // this is not possible since the VitalCommand object MUST BE A class, and classes CANNOT HAVE MULTIPE EXTEND STATEMENTS...
            this.command = new PluginCommand.Bungeecord(getName()) {
                @Override
                public void execute(net.md_5.bungee.api.CommandSender sender, String[] args) {
                    Bungeecord.this.execute(sender, args);
                }

                @Override
                public Iterable<String> onTabComplete(net.md_5.bungee.api.CommandSender sender, String[] args) {
                    return Bungeecord.this.handleTabComplete(sender, args);
                }
            };
        }

        /**
         * Registers this command.
         *
         * @param plugin The bungeecord plugin impl.
         */
        @AfterInit
        public final void afterInit(Plugin plugin) {
            plugin.getProxy().getPluginManager().registerCommand(plugin, command);
        }

        @Override
        public final boolean isPlayer(@NonNull net.md_5.bungee.api.CommandSender sender) {
            return sender instanceof ProxiedPlayer;
        }

        @Override
        public final boolean hasPermission(@NonNull net.md_5.bungee.api.CommandSender sender, @NonNull String permission) {
            return sender.hasPermission(permission);
        }

        @Override
        public List<String> getAllPlayerNames() {
            return ProxyServer.getInstance().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .toList();
        }
    }
}