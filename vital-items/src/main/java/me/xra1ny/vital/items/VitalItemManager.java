package me.xra1ny.vital.items;

import lombok.NonNull;
import lombok.extern.java.Log;
import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalComponentListManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages custom item stacks and their associated cooldowns.
 * This class is responsible for managing a collection of custom item stacks and coordinating their usage cooldowns.
 *
 * @author xRa1ny
 */
@Log
@Component
public class VitalItemManager extends VitalComponentListManager<VitalItem> {
    private static VitalItemManager instance;

    /**
     * Attempts to set the specified {@link VitalItem} by its class.
     *
     * @param inventory      The {@link Inventory} to add the item to.
     * @param itemStackClass The class of the {@link VitalItem} (must be registered).
     * @return The {@link Map} containing all items that didn't fit.
     */
    @NonNull
    public static Map<Integer, ItemStack> addItem(@NonNull Inventory inventory, @NonNull Class<? extends VitalItem> itemStackClass) {
        final VitalItem vitalItem = Optional.ofNullable(instance.getVitalComponentByClass(itemStackClass))
                .orElseThrow(() -> new RuntimeException("attempted adding unregistered itemstack %s"
                        .formatted(itemStackClass.getSimpleName())));

        return inventory.addItem(vitalItem);
    }

    /**
     * Attempts to set the specified {@link VitalItem} by its class.
     *
     * @param player         The {@link Player} to add the item to.
     * @param itemStackClass The class of the {@link VitalItem} (must be registered).
     * @return The {@link Map} containing all items that didn't fit.
     */
    @NonNull
    public static Map<Integer, ItemStack> addItem(@NonNull Player player, @NonNull Class<? extends VitalItem> itemStackClass) {
        return addItem(player.getInventory(), itemStackClass);
    }

    /**
     * Attempts to set the {@link VitalItem} by its given class to the given slot in the specified {@link Inventory}.
     *
     * @param inventory      The {@link Inventory}.
     * @param slot           The slot.
     * @param itemStackClass The class of te {@link VitalItem} (must be registered).
     */
    public static void setItem(@NonNull Inventory inventory, int slot, @NonNull Class<? extends VitalItem> itemStackClass) {
        final VitalItem vitalItem = Optional.ofNullable(instance.getVitalComponentByClass(itemStackClass))
                .orElseThrow(() -> new RuntimeException("attempted setting unregistered itemstack %s"
                        .formatted(itemStackClass.getSimpleName())));

        inventory.setItem(slot, vitalItem);
    }

    /**
     * Attempts to set the {@link VitalItem} by its given class to the given slot in the specified {@link Player}'s {@link Inventory}.
     *
     * @param player         The {@link Player}.
     * @param slot           The slot.
     * @param itemStackClass The class of te {@link VitalItem} (must be registered).
     */
    public static void setItem(@NonNull Player player, int slot, @NonNull Class<? extends VitalItem> itemStackClass) {
        setItem(player.getInventory(), slot, itemStackClass);
    }

    @Override
    public void onRegistered() {
        instance = this;
    }

    /**
     * Gets all registered items.
     *
     * @return A list of all registered items.
     */
    public List<VitalItem> getItems() {
        return instance.getVitalComponents(VitalItem.class);
    }
}