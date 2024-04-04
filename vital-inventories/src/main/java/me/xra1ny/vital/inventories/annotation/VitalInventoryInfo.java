package me.xra1ny.vital.inventories.annotation;

import lombok.NonNull;
import me.xra1ny.vital.inventories.VitalInventory;
import org.bukkit.Material;
import org.jetbrains.annotations.Range;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to provide information about a {@link VitalInventory}.
 *
 * @author xRa1ny
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VitalInventoryInfo {
    /**
     * The title of this inventory menu.
     *
     * @return The title of the inventory menu.
     */
    @NonNull
    String value();

    /**
     * The size in slots of this inventory menu. Default is 9 (one row).
     *
     * @return The size of the inventory menu.
     */
    @Range(from = 9, to = 54)
    int size() default 9;

    /**
     * The material used as the background of this inventory menu. Default is AIR.
     *
     * @return The background material.
     */
    @NonNull
    Material background() default Material.AIR;
}