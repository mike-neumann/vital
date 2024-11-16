package me.vitalframework.commands.annotation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.vitalframework.commands.VitalCommand;
import org.bukkit.Material;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Annotation used to define arguments for {@link VitalCommand}.
 * Arguments may be placeholders that can be used within methods annotated with {@link VitalCommandArgHandler}.
 * For example, "%PLAYER%" will be replaced with all player names on the server.
 *
 * @author xRa1ny
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VitalCommandArg {
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
     * Patterns used by {@link VitalCommandArg} implementations that will be replaced during tab-completion, automatically.
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