package me.xra1ny.vital.inventories.annotation;

import org.jetbrains.annotations.Range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VitalPagedInventoryInfo {
    /**
     * Defines the starting slot for each page item.
     *
     * @return The starting slot for each page item.
     */
    @Range(from = 0, to = 9)
    int fromSlot() default 0;

    /**
     * Defines the ending slot for each page item.
     *
     * @return The ending slot for each page item.
     */
    @Range(from = 0, to = 9)
    int toSlot() default 0;
}