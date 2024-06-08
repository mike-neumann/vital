package me.xra1ny.vital.commands.annotation;

import me.xra1ny.vital.commands.VitalCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
     * Pattern used by {@link VitalCommandArg} implementations that will be replaced by all player names during tab-completion, automatically.
     */
    String PLAYER = "%PLAYER%";

    /**
     * Pattern used by {@link VitalCommandArg} implementations that will be replaced by "true" or "false" during tab-completion, automatically.
     */
    String BOOLEAN = "%BOOLEAN%";

    /**
     * Pattern used by {@link VitalCommandArg} implementations that will be replaced by "0" during tab-completion, automatically.
     */
    String NUMBER = "%NUMBER%";

    /**
     * Pattern used by {@link VitalCommandArg} implementations that will be replaced by all available minecraft materials during tab-completion, automatically.
     */
    String MATERIAL = "%MATERIAL%";

    /**
     * Placeholder value for the command argument.
     * Examples include "%PLAYER%", "%BOOLEAN%", and "%NUMBER%".
     *
     * @return The value of the command argument.
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
}