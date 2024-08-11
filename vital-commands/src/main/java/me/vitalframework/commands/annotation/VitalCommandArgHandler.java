package me.vitalframework.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to specify methods as handlers for {@link VitalCommandArg}.
 * Handlers are responsible for processing specific command argument values.
 * This annotation helps map methods to their corresponding command argument values.
 *
 * @author xRa1ny
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VitalCommandArgHandler {
    /**
     * Array of command argument values that this handler method processes.
     * Each value corresponds to a specific command argument.
     *
     * @return An array of command argument values.
     */
    String[] value();
}
