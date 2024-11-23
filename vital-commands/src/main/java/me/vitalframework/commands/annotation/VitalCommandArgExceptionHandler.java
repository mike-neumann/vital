package me.vitalframework.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Decorator annotation for marking exception handling methods for registered commands beans
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VitalCommandArgExceptionHandler {
    /**
     * Defines the command arg this exception handling method should be mapped to
     */
    String value();
}