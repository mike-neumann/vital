package me.vitalframework.items;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Manages custom item stacks and their associated cooldowns.
 * This class is responsible for managing a collection of custom item stacks and coordinating their usage cooldowns.
 *
 * @author xRa1ny
 */
@Component
public class VitalItemManager {
    /**
     * Attempts to set the specified {@link VitalItem} by its class.
     *
     * @param inventory      The {@link Inventory} to add the item to.
     * @param itemStackClass The class of the {@link VitalItem} (must be registered).
     * @return The {@link Map} containing all items that didn't fit.
     */
    @NonNull
    public Map<Integer, ItemStack> addItem(@NonNull Inventory inventory, @NonNull Class<? extends VitalItem> itemStackClass) {
        final VitalItem vitalItem = Vital.getContext().getBean(itemStackClass);

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
    public Map<Integer, ItemStack> addItem(@NonNull Player player, @NonNull Class<? extends VitalItem> itemStackClass) {
        return addItem(player.getInventory(), itemStackClass);
    }

    /**
     * Attempts to set the {@link VitalItem} by its given class to the given slot in the specified {@link Inventory}.
     *
     * @param inventory      The {@link Inventory}.
     * @param slot           The slot.
     * @param itemStackClass The class of te {@link VitalItem} (must be registered).
     */
    public void setItem(@NonNull Inventory inventory, int slot, @NonNull Class<? extends VitalItem> itemStackClass) {
        final VitalItem vitalItem = Vital.getContext().getBean(itemStackClass);

        inventory.setItem(slot, vitalItem);
    }

    /**
     * Attempts to set the {@link VitalItem} by its given class to the given slot in the specified {@link Player}'s {@link Inventory}.
     *
     * @param player         The {@link Player}.
     * @param slot           The slot.
     * @param itemStackClass The class of te {@link VitalItem} (must be registered).
     */
    public void setItem(@NonNull Player player, int slot, @NonNull Class<? extends VitalItem> itemStackClass) {
        setItem(player.getInventory(), slot, itemStackClass);
    }

    public List<VitalItem> getItems() {
        return Vital.getContext().getBeansOfType(VitalItem.class)
                .values()
                .stream()
                .toList();
    }
}