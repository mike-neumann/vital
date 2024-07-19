package me.xra1ny.vital.tasks.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VitalCountdownTaskInfo {
    /**
     * Defines the countdown for this annotated countdown task in seconds.
     *
     * @return The countdown in seconds
     */
    int countdown();

    /**
     * Defines the interval between countdown task ticks in milliseconds.
     *
     * @return The interval in milliseconds.
     */
    int interval() default 1_000;
}
