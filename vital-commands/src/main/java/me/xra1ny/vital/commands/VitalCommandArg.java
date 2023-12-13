package me.xra1ny.vital.commands;

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
    String PLAYER = "%PLAYER%";
    String BOOLEAN = "%BOOLEAN%";
    String NUMBER = "%NUMBER%";

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
