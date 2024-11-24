package me.vitalframework.commands;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vitalframework.RequiresAnnotation;
import me.vitalframework.commands.crossplatform.VitalPluginCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
public abstract class VitalCommand<P, CS> implements RequiresAnnotation<VitalCommand.Info> {
    private final Class<CS> commandSenderClass;

    @Getter
    private final String name;

    @Getter
    private final String permission;

    @Getter
    private final boolean requiresPlayer;
    @Getter
    private final Map<Pattern, Arg> args;
    @Getter
    private final Map<Arg, ArgHandlerContext> argHandlers;
    @Getter
    private final Map<Arg, ArgExceptionHandlerContext> argExceptionHandlers;
    @Getter
    @Autowired
    private P plugin;

    /**
     * Constructor for when using dependency injection
     */
    protected VitalCommand(@NonNull Class<CS> commandSenderClass) {
        final var vitalCommandInfo = getRequiredAnnotation();

        this.commandSenderClass = commandSenderClass;
        name = vitalCommandInfo.name();
        permission = vitalCommandInfo.permission();
        requiresPlayer = vitalCommandInfo.requiresPlayer();
        args = getMappedArgs();
        argHandlers = getMappedArgHandlers();
        argExceptionHandlers = getMappedArgExceptionHandlers();
    }

    /**
     * Constructor for when not using dependency injection
     */
    protected VitalCommand(@NonNull P plugin, @NonNull Class<CS> commandSenderClass) {
        final var vitalCommandInfo = getRequiredAnnotation();

        this.plugin = plugin;
        this.commandSenderClass = commandSenderClass;
        name = vitalCommandInfo.name();
        permission = vitalCommandInfo.permission();
        requiresPlayer = vitalCommandInfo.requiresPlayer();
        args = getMappedArgs();
        argHandlers = getMappedArgHandlers();
        argExceptionHandlers = getMappedArgExceptionHandlers();
    }

    private Map<Pattern, Arg> getMappedArgs() {
        return Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(ArgHandler.class))
                .map(method -> method.getAnnotation(ArgHandler.class))
                .map(handler -> Map.entry(
                        Pattern.compile(
                                handler.value().value().replaceAll(" ", "[ ]")
                                        .replaceAll("%.+%[*]", "(.+)")
                                        .replaceAll("%.+%", "(\\\\S+)")
                        ),
                        handler.value()
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Arg, ArgHandlerContext> getMappedArgHandlers() {
        return Arrays.stream(getClass().getMethods())
                .filter(method -> ReturnState.class.isAssignableFrom(method.getReturnType()))
                .filter(method -> method.isAnnotationPresent(ArgHandler.class))
                .map(method -> {
                    // now we have a viable method ready for handling incoming arguments
                    // we just need to filter out the injectable parameters for our method
                    // since we only support a handful of injectable params for handler methods...
                    final var vitalCommandArg = method.getAnnotation(ArgHandler.class).value();
                    final var parameters = List.of(method.getParameters());
                    Integer commandSenderIndex = null;
                    Integer executedArgIndex = null;
                    Integer commandArgIndex = null;
                    Integer valuesIndex = null;

                    for (var parameter : parameters) {
                        if (commandSenderClass.isAssignableFrom(parameter.getType())) {
                            commandSenderIndex = parameters.indexOf(parameter);
                        } else if (String.class.isAssignableFrom(parameter.getType())) {
                            executedArgIndex = parameters.indexOf(parameter);
                        } else if (Arg.class.isAssignableFrom(parameter.getType())) {
                            commandArgIndex = parameters.indexOf(parameter);
                        } else if (String[].class.isAssignableFrom(parameter.getType())) {
                            valuesIndex = parameters.indexOf(parameter);
                        }
                    }

                    return Map.entry(vitalCommandArg, new ArgHandlerContext(method, commandSenderIndex, executedArgIndex, commandArgIndex, valuesIndex));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Arg, ArgExceptionHandlerContext> getMappedArgExceptionHandlers() {
        return Arrays.stream(getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(ArgExceptionHandler.class))
                .map(method -> {
                    final var commandArgExceptionHandler = method.getAnnotation(ArgExceptionHandler.class);
                    final var arg = getArg(commandArgExceptionHandler.value());

                    if (arg == null) {
                        throw new IllegalArgumentException("Exception handler mapping failed, arg '" + commandArgExceptionHandler.value() + "' does not exist");
                    }

                    final var parameters = List.of(method.getParameters());
                    Integer commandSenderIndex = null;
                    Integer executedArgIndex = null;
                    Integer commandArgIndex = null;
                    Integer exceptionIndex = null;

                    for (var parameter : parameters) {
                        if (commandSenderClass.isAssignableFrom(parameter.getType())) {
                            commandSenderIndex = parameters.indexOf(parameter);
                        } else if (String.class.isAssignableFrom(parameter.getType())) {
                            executedArgIndex = parameters.indexOf(parameter);
                        } else if (Arg.class.isAssignableFrom(parameter.getType())) {
                            commandArgIndex = parameters.indexOf(parameter);
                        } else if (Exception.class.isAssignableFrom(parameter.getType())) {
                            exceptionIndex = parameters.indexOf(parameter);
                        }
                    }

                    return Map.entry(arg, new ArgExceptionHandlerContext(method, commandSenderIndex, executedArgIndex, commandArgIndex, exceptionIndex));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public final Class<Info> requiredAnnotationType() {
        return Info.class;
    }

    public final Arg getArg(@NonNull String arg) {
        return args.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(arg).matches())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private void executeArgExceptionHandlerMethod(@NonNull CS sender, @NonNull Exception exception, @NonNull String arg, @NonNull Arg commandArg, @NonNull String[] values) {
        final var exceptionHandlerContext = argExceptionHandlers.get(commandArg);

        // we may or may not have an exception handler mapped to this command argument
        if (exceptionHandlerContext != null) {
            final var injectableParameters = new HashMap<Integer, Object>();

            if (exceptionHandlerContext.commandSenderIndex != null) {
                injectableParameters.put(exceptionHandlerContext.commandSenderIndex, sender);
            }

            if (exceptionHandlerContext.executedArgIndex != null) {
                injectableParameters.put(exceptionHandlerContext.executedArgIndex, arg);
            }

            if (exceptionHandlerContext.argIndex != null) {
                injectableParameters.put(exceptionHandlerContext.argIndex, commandArg);
            }

            if (exceptionHandlerContext.exceptionIndex != null) {
                injectableParameters.put(exceptionHandlerContext.exceptionIndex, exception);
            }

            try {
                final var sortedParameters = injectableParameters.entrySet().stream().sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .toArray();

                exceptionHandlerContext.handlerMethod.invoke(this, sortedParameters);
            } catch (Exception e) {
                log.error("Error while executing exception handler method using context {}", exceptionHandlerContext, e);
            }
        }
    }

    @NonNull
    private VitalCommand.ReturnState executeArgHandlerMethod(@NonNull CS sender, @NonNull String arg, @NonNull Arg commandArg, @NonNull String @NonNull [] values) throws InvocationTargetException, IllegalAccessException {
        final var argHandlerContext = argHandlers.get(commandArg);

        if (argHandlerContext == null) {
            throw new IllegalArgumentException("No handler method exists for arg '" + arg + "'");
        }

        final var injectableParameters = new HashMap<Integer, Object>();

        if (argHandlerContext.commandSenderIndex != null) {
            injectableParameters.put(argHandlerContext.commandSenderIndex, sender);
        }

        if (argHandlerContext.executedArgIndex != null) {
            injectableParameters.put(argHandlerContext.executedArgIndex, arg);
        }

        if (argHandlerContext.commandArgIndex != null) {
            injectableParameters.put(argHandlerContext.commandArgIndex, commandArg);
        }

        if (argHandlerContext.valuesIndex != null) {
            injectableParameters.put(argHandlerContext.valuesIndex, values);
        }

        final var sortedParameters = injectableParameters.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .toArray();

        return (ReturnState) argHandlerContext.handlerMethod.invoke(this, sortedParameters);
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

        for (var arg : this.args.values()) {
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

                final var commandArgType = Arg.Type.getTypeByPlaceholder(finalArg);

                if (commandArgType != null) {
                    commandArgType.getConsumer().accept(new Arg.Type.TabContext(
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

        // the arguments the player has typed in chat, joined to one single string separated by spaces
        final var joinedPlayerArgs = String.join(" ", args);
        // find executing command arg
        final var executingArg = getArg(joinedPlayerArgs);
        ReturnState commandReturnState;

        // extract user values from command
        final var values = new ArrayList<String>();


        if (executingArg == null) {
            commandReturnState = onBaseCommand(sender);
        } else {
            for (var commandArg : executingArg.value().split(" ")) {
                for (var userArg : args) {
                    if (!userArg.equalsIgnoreCase(commandArg)) {
                        // we have a custom arg
                        values.add(userArg);
                    }
                }
            }

            try {
                commandReturnState = executeArgHandlerMethod(sender, joinedPlayerArgs, executingArg, values.toArray(String[]::new));
            } catch (Exception e) {
                executeArgExceptionHandlerMethod(sender, e, joinedPlayerArgs, executingArg, values.toArray(String[]::new));

                return;
            }
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
    protected VitalCommand.ReturnState onBaseCommand(@NonNull CS sender) {
        return ReturnState.INVALID_ARGS;
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
     * Called when this VitalCommand has been executed without needed Permissions
     *
     * @param sender The CommandSender
     */
    protected void onCommandRequiresPermission(@NonNull CS sender, @NonNull String args, Arg arg) {

    }

    /**
     * Called when this VitalCommand has been executed as a non Player Object while requiring a Player to be executed
     *
     * @param sender The CommandSender
     */
    protected void onCommandRequiresPlayer(@NonNull CS sender, @NonNull String args, Arg arg) {

    }

    /**
     * Enum representing possible return states for {@link VitalCommand}.
     * Defines different states that a command execution can result in.
     *
     * @author xRa1ny
     */
    public enum ReturnState {
        /**
         * Indicates that the command was executed with invalid arguments.
         */
        INVALID_ARGS,

        /**
         * Indicates that the command execution was successful.
         */
        SUCCESS,

        /**
         * Indicates that the sender did not have permission to execute the command.
         */
        NO_PERMISSION
    }

    /**
     * Annotation used to provide metadata for {@link VitalCommand}.
     *
     * @author xRa1ny
     * @apiNote If combined with the :vital-core-processor and :vital-commands-processor dependency as annotation processor, can automatically define all commands in plugin.yml during compile-time.
     */
    @Component
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        /**
         * Defines the name of this command, excluding the slash /.
         *
         * @return The name of the command.
         */
        String name();

        /**
         * Defines the description of this command.
         *
         * @return The description of this command.
         */
        String description() default "A Vital Command";

        /**
         * The aliases of this command.
         *
         * @return The aliases of this command.
         */
        String[] aliases() default {};

        /**
         * The usages message of this command.
         *
         * @return The usages message of this command.
         */
        String usage() default "";

        /**
         * Defines the permission required to run this command.
         *
         * @return The required permission (default is an empty string).
         */
        String permission() default "";

        /**
         * Defines if this command can only be executed by a player.
         *
         * @return True if the command requires a player; false otherwise (default is true).
         */
        boolean requiresPlayer() default true;
    }

    /**
     * Annotation used to define arguments for {@link VitalCommand}.
     * Arguments may be placeholders that can be used within methods annotated with {@link ArgHandler}.
     * For example, "%PLAYER%" will be replaced with all player names on the server.
     *
     * @author xRa1ny
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Arg {
        /**
         * Placeholder value for the command argument.
         *
         * @return The value of the command argument.
         * @see Type
         */
        String value();

        /**
         * Optional permission node associated with the command argument.
         * Defines the required permission to use this argument in a command.
         *
         * @return The permission node (default is an empty string).
         */
        String permission() default "";

        /**
         * Flag indicating if this argument is specific to players.
         *
         * @return True if the argument is for players only; false otherwise (default is false).
         * @apiNote If set to true, the argument is only applicable to player senders.
         */
        boolean player() default false;

        /**
         * Patterns used by {@link Arg} implementations that will be replaced during tab-completion, automatically.
         */
        @Getter
        @RequiredArgsConstructor
        enum Type {
            PLAYER("%PLAYER%", context -> {
                context.playerNames.stream()
                        // Check if the player name is already in the tabCompleted list.
                        .filter(Predicate.not(context.tabCompleted::contains))
                        .forEach(context.tabCompleted::add);
            }),
            BOOLEAN("%BOOLEAN%", context -> {
                context.tabCompleted.add("true");
                context.tabCompleted.add("false");
            }),
            NUMBER("%NUMBER%", context -> {
                context.tabCompleted.add("0");
            }),
            MATERIAL("%MATERIAL%", context -> {
                Arrays.stream(Material.values())
                        .map(Material::name)
                        .forEach(context.tabCompleted::add);
            });

            private final String placeholder;
            private final Consumer<TabContext> consumer;

            public static Type getTypeByPlaceholder(String placeholder) {
                return Arrays.stream(Type.values())
                        .filter(type -> type.placeholder.equals(placeholder))
                        .findFirst()
                        .orElse(null);
            }

            /**
             * Defines the context during tab completion conversion of the above placeholder types
             */
            public record TabContext(
                    List<String> tabCompleted,
                    List<String> playerNames
            ) {
            }
        }
    }

    /**
     * Annotation used to specify methods as handlers for {@link Arg}.
     * Handlers are responsible for processing specific command argument values.
     * This annotation helps map methods to their corresponding command argument values.
     *
     * @author xRa1ny
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ArgHandler {
        /**
         * Array of command argument values that this handler method processes.
         * Each value corresponds to a specific command argument.
         *
         * @return An array of command argument values.
         */
        Arg value();
    }

    /**
     * Decorator annotation for marking exception handling methods for registered commands beans
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ArgExceptionHandler {
        /**
         * Defines the command arg this exception handling method should be mapped to
         */
        String value();
    }

    public record ArgHandlerContext(
            Method handlerMethod,
            Integer commandSenderIndex,
            Integer executedArgIndex,
            Integer commandArgIndex,
            Integer valuesIndex
    ) {
    }

    public record ArgExceptionHandlerContext(
            Method handlerMethod,
            Integer commandSenderIndex,
            Integer executedArgIndex,
            Integer argIndex,
            Integer exceptionIndex
    ) {
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