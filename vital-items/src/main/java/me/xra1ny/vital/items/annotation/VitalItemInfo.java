package me.xra1ny.vital.items.annotation;

import lombok.NonNull;
import me.xra1ny.vital.items.VitalItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide information about a {@link VitalItem} that can be interacted with in the game.
 *
 * @author xRa1ny
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VitalItemInfo {
    /**
     * Defines the name of the custom item stack.
     *
     * @return The name of the item stack.
     */
    String name();

    /**
     * Defines an array of lore lines for the custom item stack.
     * Lore lines provide additional information about the item.
     *
     * @return An array of lore lines.
     */
    @NonNull
    String[] lore() default {};

    /**
     * Defines the amount of items in the stack (stack size).
     *
     * @return The stack size, which is the number of items in the stack.
     */
    int amount() default 1;

    /**
     * Specifies the material or type of the custom item stack.
     *
     * @return The material/type of the item stack, represented by the Material enum.
     */
    Material type();

    /**
     * Defines an array of item flags that control specific display properties of the item stack.
     * Item flags can be used to hide attributes, enchantments, and more.
     *
     * @return An array of item flags affecting the item's display.
     */
    ItemFlag[] itemFlags() default {};

    /**
     * Defines the cooldown time (in seconds) for the custom item stack's interaction.
     * The cooldown restricts how frequently the item can be used.
     *
     * @return The cooldown time in seconds. A value of 0 indicates no cooldown.
     */
    int cooldown() default 0;

    /**
     * Indicates whether the custom item stack should have enchantment visual effects.
     *
     * @return True if the item should appear as if enchanted; false otherwise.
     */
    boolean enchanted() default false;

    /**
     * Indicates whether the custom item stack should be unbreakable.
     *
     * @return True if the item should be unbreakable; false otherwise.
     */
    boolean unbreakable() default true;
}