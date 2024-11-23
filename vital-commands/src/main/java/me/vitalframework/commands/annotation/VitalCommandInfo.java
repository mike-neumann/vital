package me.vitalframework.commands.annotation;

import me.vitalframework.commands.VitalCommand;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to provide metadata for {@link VitalCommand}.
 *
 * @author xRa1ny
 * @apiNote If combined with the :vital-core-processor and :vital-commands-processor dependency as annotation processor, can automatically define all commands in plugin.yml during compile-time.
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VitalCommandInfo {
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