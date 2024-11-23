package me.vitalframework.commands;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import me.vitalframework.RequiresAnnotation;
import me.vitalframework.commands.annotation.VitalCommandArg;
import me.vitalframework.commands.annotation.VitalCommandArgHandler;
import me.vitalframework.commands.annotation.VitalCommandInfo;
import me.vitalframework.commands.crossplatform.VitalPluginCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base class for custom Minecraft commands using the Vital framework.
 * Provides functionality for command execution, tab completion, and argument handling.
 *
 * @param <CS> The command sender type of this command.
 * @author xRa1ny
 */
@Slf4j
public abstract class VitalCommand<P, CS> implements RequiresAnnotation<VitalCommandInfo> {
    public record ExecutionContext(
            String executedArg,
            String[] values,
            VitalCommandArg arg
    ) {
    }

    public record ArgExceptionHandlerContext(
            Method handlerMethod,
            Map<Integer, Object> injectableParams,
            Throwable e
    ) {
    }

    public record ArgHandlerContext(
            Method handlerMethod,
            Map<Integer, Object> injectableParams
    ) {
    }

    private final Class<CS> commandSenderClass;

    @Getter
    private final String name;

    @Getter
    private final String permission;

    @Getter
    private final boolean requiresPlayer;

    @Getter
    @Autowired
    private P plugin;

    @Getter
    private final Map<Pattern, VitalCommandArg> vitalCommandArgs;


    @Getter
    private final Map<VitalCommandArg, ArgHandlerContext> vitalCommandArgHandlers;

    @Getter
    private final Map<VitalCommandArg, ArgExceptionHandlerContext> vitalCommandArgExceptionHandlers;

    /**
     * Constructor for when using dependency injection
     */
    protected VitalCommand(@NonNull Class<CS> commandSenderClass) {
        final VitalCommandInfo vitalCommandInfo = getRequiredAnnotation();


        this.commandSenderClass = commandSenderClass;
        name = vitalCommandInfo.name();
        permission = vitalCommandInfo.permission();
        requiresPlayer = vitalCommandInfo.requiresPlayer();
        vitalCommandArgs = getMappedArgs(vitalCommandInfo);
        vitalCommandArgHandlers = getMappedCommandArgHandlers(vitalCommandArgs.values());
        vitalCommandArgExceptionHandlers = getMappedCommandArgExceptionHandlers(vitalCommandArgs.values());
    }

    /**
     * Constructor for when not using dependency injection
     */
    protected VitalCommand(@NonNull P plugin, @NonNull Class<CS> commandSenderClass) {
        final VitalCommandInfo vitalCommandInfo = getRequiredAnnotation();

        this.plugin = plugin;
        this.commandSenderClass = commandSenderClass;
        name = vitalCommandInfo.name();
        permission = vitalCommandInfo.permission();
        requiresPlayer = vitalCommandInfo.requiresPlayer();
        vitalCommandArgs = getMappedArgs(vitalCommandInfo);
    }

    private Map<Pattern, VitalCommandArg> getMappedArgs() {
        return Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(VitalCommandArg.class))
                .map(method -> method.getAnnotation(VitalCommandArg.class))
                .map(arg -> Map.entry(
                        Pattern.compile(
                                arg.value().replaceAll(" ", "[ ]")
                                        .replaceAll("%.+%[*]", "(.+)")
                                        .replaceAll("%.+%", "(\\\\S+)")
                        ),
                        arg
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<VitalCommandArg, Method> getMappedCommandArgHandlers() {
        return Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(VitalCommandArg.class))
                .map(method -> {
                    // now we have a viable method ready for handling incoming arguments
                    // we just need to filter out the injectable parameters for our method
                    // since we only support a handful of injectable params for handler methods...
                    final var vitalCommandArg = method.getAnnotation(VitalCommandArg.class);



                    return Map.entry(vitalCommandArg, method);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<VitalCommandArg, Method> getMappedCommandArgExceptionHandlers(@NonNull Collection<VitalCommandArg> vitalCommandArgs) {

    }

    @Override
    public final Class<VitalCommandInfo> requiredAnnotationType() {
        return VitalCommandInfo.class;
    }

    public final VitalCommandArg getExecutingArg(@NonNull String arg) {
        return vitalCommandArgs.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(arg).matches())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @NonNull
    private VitalCommandReturnState executeCommandArgHandlerMethod(@NonNull CS sender, @NonNull VitalCommandArg commandArg, @NonNull String @NonNull [] values) throws InvocationTargetException, IllegalAccessException {
        // Initialize a variable to hold the handler method.
        Method commandArgHandlerMethod = null;

        // Iterate through the methods of the current class.
        for (var method : getClass().getDeclaredMethods()) {
            // Retrieve the `@VitalCommandArgHandler` annotation for the method.
            final var commandArgHandler = method.getAnnotation(VitalCommandArgHandler.class);

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
        final var injectedParameters = new ArrayList<>();

        for (var parameter : commandArgHandlerMethod.getParameters()) {
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
     * Handles the tab complete action on any command sender.
     *
     * @param sender The sender.
     * @param args   Any args used during tab completion.
     * @return A list of all supported tab completions.
     */
    @NonNull
    protected final List<String> handleTabComplete(@NonNull CS sender, @NonNull String @NonNull [] args) {
        final var tabCompleted = new ArrayList<String>();

        for (var arg : vitalCommandArgs.values()) {
            // Split the value of the command argument into individual parts.
            final var originalArgs = arg.value().split(" ");
            // Clone the originalArgs to avoid modification.
            final var editedArgs = originalArgs.clone();

            // Check if the originalArgs length is greater than or equal to the provided args length
            // or if the last element of originalArgs ends with "%*".
            if (originalArgs.length >= args.length || originalArgs[originalArgs.length - 1].endsWith("%*")) {
                for (var i = 0; i < args.length; i++) {
                    // Determine the original argument at the current index.
                    final var originalArg = i >= originalArgs.length ? originalArgs[originalArgs.length - 1] : originalArgs[i];

                    if (!originalArg.startsWith("%") && !(originalArg.endsWith("%") || originalArg.endsWith("%*"))) {
                        continue;
                    }

                    // Replace the edited argument at the corresponding index with the provided argument.
                    editedArgs[i >= editedArgs.length ? editedArgs.length - 1 : i] = args[i];
                }

                // Determine the final argument from originalArgs and args.
                final var finalArg = originalArgs[args.length - 1 >= originalArgs.length ? originalArgs.length - 1 : args.length - 1];

                // Check if the joined editedArgs start with the joined provided args.
                if (!String.join(" ", editedArgs).startsWith(String.join(" ", args))) {
                    continue;
                }

                if (finalArg.startsWith("%") && finalArg.endsWith("%*")) {
                    // Add the final argument with "%" and "%*" removed to the tabCompleted list.
                    tabCompleted.add(finalArg.replace("%", "").replace("%*", ""));

                    break;
                }

                final var commandArgType = VitalCommandArg.Type.getTypeByPlaceholder(finalArg);

                if (commandArgType != null) {
                    commandArgType.getConsumer().accept(new VitalCommandArg.Type.TabContext(
                            tabCompleted,
                            getAllPlayerNames()
                    ));
                } else if (finalArg.startsWith("%") && (finalArg.endsWith("%") || finalArg.endsWith("%*"))) {
                    tabCompleted.add(finalArg.replace("%", "").replace("%*", ""));
                } else {
                    tabCompleted.add(finalArg);
                }
            }
        }

        final var formattedArgs = String.join(" ", Stream.of(args)
                .map(arg -> "?")
                .toList());
        final var commandTabCompleted = onCommandTabComplete(sender, formattedArgs);

        // when our OWN implementation is not empty, clear all of Vital's defaults.
        if (!commandTabCompleted.isEmpty()) {
            tabCompleted.clear();
        }

        // finally add further tab-completed suggestions implemented by the developer.
        tabCompleted.addAll(commandTabCompleted);

        return tabCompleted;
    }

    /**
     * If this generic sender is of type player.
     *
     * @param commandSender The sender.
     * @return True if the sender is a player; false otherwise.
     */
    public abstract boolean isPlayer(@NonNull CS commandSender);

    /**
     * Checks if the given sender has the required permissions to run this c command.
     *
     * @param commandSender The sender.
     * @param permission    The permission to check for.
     * @return True if the sender is permitted; false otherwise.
     */
    public abstract boolean hasPermission(@NonNull CS commandSender, @NonNull String permission);

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
     */
    public final void execute(@NonNull CS sender, @NonNull String[] args) {
        // Check if the command requires a player sender.
        if (requiresPlayer) {
            // Check if the sender is not a Player.
            if (!isPlayer(sender)) {
                // Execute the onCommandRequiresPlayer method and return true.
                onCommandRequiresPlayer(sender, String.join(" ", args), null);

                return;
            }
        }

        // Check if a permission is required and if the sender has it.
        if (!permission.isBlank() && !hasPermission(sender, permission)) {
            // Execute the onCommandRequiresPermission method and return true.
            onCommandRequiresPermission(sender, String.join(" ", args), null);

            return;
        }

        // find executing command arg
        final var executingArg = getExecutingArg(String.join(" ", args));
        VitalCommandReturnState commandReturnState;

        try {
            if (executingArg == null) {
                commandReturnState = onBaseCommand(sender);
            } else {
                // extract user values from command
                final var values = new ArrayList<String>();

                for (var commandArg : executingArg.value().split(" ")) {
                    for (var userArg : args) {
                        if (!userArg.equalsIgnoreCase(commandArg)) {
                            // we have a custom arg
                            values.add(userArg);
                        }
                    }
                }

                commandReturnState = executeCommandArgHandlerMethod(sender, executingArg, values.toArray(String[]::new));
            }
        } catch (Exception e) {
            onCommandError(sender, String.join(" ", args), executingArg, e);

            return;
        }

        final String joinedArgs = String.join(" ", args);

        // Handle the command return state.
        switch (commandReturnState) {
            case INVALID_ARGS -> onCommandInvalidArgs(sender, joinedArgs);
            case NO_PERMISSION -> onCommandRequiresPermission(sender, joinedArgs, executingArg);
        }
    }

    /**
     * called when this command is executed with only the base command (/commandname)
     *
     * @param sender the sender
     * @return the status of this command execution
     */
    @NonNull
    protected VitalCommandReturnState onBaseCommand(@NonNull CS sender) {
        return VitalCommandReturnState.INVALID_ARGS;
    }

    /**
     * Called upon requesting any tab-completion content.
     *
     * @param sender The {@link CS} that sent the command.
     * @param args   The arguments used in chat.
     * @return A {@link List} of strings to show to the player as tab-completion suggestions.
     */
    @NonNull
    protected List<String> onCommandTabComplete(@NonNull CS sender, @NonNull String args) {
        return List.of();
    }

    /**
     * Called when this VitalCommand has been executed using invalid Arguments
     *
     * @param sender The CommandSender
     */
    protected void onCommandInvalidArgs(@NonNull CS sender, @NonNull String args) {

    }

    /**
     * Called when this VitalCommand has been executed and an Error has occurred (Exception was thrown)
     *
     * @param sender The CommandSender
     */
    protected void onCommandError(@NonNull CS sender, @NonNull String args, VitalCommandArg arg, @NonNull Exception e) {

    }

    /**
     * Called when this VitalCommand has been executed without needed Permissions
     *
     * @param sender The CommandSender
     */
    protected void onCommandRequiresPermission(@NonNull CS sender, @NonNull String args, VitalCommandArg arg) {

    }

    /**
     * Called when this VitalCommand has been executed as a non Player Object while requiring a Player to be executed
     *
     * @param sender The CommandSender
     */
    protected void onCommandRequiresPlayer(@NonNull CS sender, @NonNull String args, VitalCommandArg arg) {

    }

    public static abstract class Spigot extends VitalCommand<JavaPlugin, org.bukkit.command.CommandSender> implements VitalPluginCommand.Spigot {
        public Spigot() {
            super(org.bukkit.command.CommandSender.class);
        }

        public Spigot(@NonNull JavaPlugin plugin) {
            super(plugin, org.bukkit.command.CommandSender.class);
        }

        @PostConstruct
        public final void init() {
            getPlugin().getCommand(getName()).setExecutor(this);
        }

        @Override
        public final boolean onCommand(@NonNull org.bukkit.command.CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
            execute(sender, args);

            return true;
        }

        @Override
        public final List<String> onTabComplete(@NonNull org.bukkit.command.CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
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

    public static abstract class Bungeecord extends VitalCommand<Plugin, net.md_5.bungee.api.CommandSender> {
        private VitalPluginCommand.Bungeecord command;

        public Bungeecord() {
            super(net.md_5.bungee.api.CommandSender.class);

            setupCommand();
        }

        public Bungeecord(@NonNull Plugin plugin) {
            super(plugin, net.md_5.bungee.api.CommandSender.class);

            setupCommand();
        }

        private void setupCommand() {
            // wrap a custom bungeecord command class, since in bungeecord, command classes MUST BE EXTENDED FROM.
            // extending is not possible here since the VitalCommand object MUST BE A class, and classes CANNOT HAVE MULTIPLE EXTEND STATEMENTS...
            this.command = new VitalPluginCommand.Bungeecord(getName()) {
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

        @PostConstruct
        public final void init() {
            getPlugin().getProxy().getPluginManager().registerCommand(getPlugin(), command);
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