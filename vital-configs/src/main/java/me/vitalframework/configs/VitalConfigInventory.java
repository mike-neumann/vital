package me.vitalframework.configs;

import lombok.NonNull;
import me.vitalframework.configs.annotation.VitalConfigProperty;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

/**
 * Wrapper class to store inventory data to a config file.
 *
 * @author xRa1ny
 */
public class VitalConfigInventory {
    @VitalConfigProperty(InventoryType.class)
    public InventoryType type;

    @VitalConfigProperty(VitalConfigItemStack.class)
    public VitalConfigItemStack[] contents;

    @NonNull
    public static VitalConfigInventory of(@NonNull Inventory inventory) {
        final var vitalConfigInventory = new VitalConfigInventory();

        vitalConfigInventory.type = inventory.getType();
        vitalConfigInventory.contents = Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .map(VitalConfigItemStack::of)
                .toArray(VitalConfigItemStack[]::new);

        return vitalConfigInventory;
    }

    @NonNull
    public Inventory toInventory(@NonNull InventoryHolder holder) {
        final var inventory = Bukkit.createInventory(holder, type);

        inventory.setContents(Arrays.stream(contents)
                .map(VitalConfigItemStack::toItemStack)
                .toArray(ItemStack[]::new));

        return inventory;
    }
}
