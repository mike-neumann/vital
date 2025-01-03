package me.vitalframework.items;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manages custom item stacks and their associated cooldowns.
 * This class is responsible for managing a collection of custom item stacks and coordinating their usage cooldowns.
 */
@Service
public class VitalItemService {
    /**
     * Attempts to set the specified item by its class.
     */
    @NonNull
    public Map<Integer, ItemStack> addItem(@NonNull Inventory inventory, @NonNull Class<? extends VitalItem> itemStackClass) {
        final var vitalItem = Vital.INSTANCE.getContext().getBean(itemStackClass);

        return inventory.addItem(vitalItem);
    }

    /**
     * Attempts to add the specified item by its class.
     */
    @NonNull
    public Map<Integer, ItemStack> addItem(@NonNull Player player, @NonNull Class<? extends VitalItem> itemStackClass) {
        return addItem(player.getInventory(), itemStackClass);
    }

    /**
     * Attempts to set the item by its given class to the given slot in the specified inventory.
     */
    public void setItem(@NonNull Inventory inventory, int slot, @NonNull Class<? extends VitalItem> itemStackClass) {
        final VitalItem vitalItem = Vital.INSTANCE.getContext().getBean(itemStackClass);

        inventory.setItem(slot, vitalItem);
    }

    /**
     * Attempts to set the item by its given class to the given slot in the specified player inventory.
     */
    public void setItem(@NonNull Player player, int slot, @NonNull Class<? extends VitalItem> itemStackClass) {
        setItem(player.getInventory(), slot, itemStackClass);
    }

    @NonNull
    public Collection<? extends VitalItem> getItems(@NonNull Class<? extends VitalItem> vitalItemClass) {
        try {
            return Vital.INSTANCE.getContext().getBeansOfType(vitalItemClass).values();
        } catch (Exception e) {
            return List.of();
        }
    }

    @NonNull
    public Collection<? extends VitalItem> getItems() {
        return getItems(VitalItem.class);
    }


    public <T extends VitalItem> T getItem(@NonNull Class<T> vitalItemClass) {
        try {
            return Vital.INSTANCE.getContext().getBean(vitalItemClass);
        } catch (Exception e) {
            return null;
        }
    }
}