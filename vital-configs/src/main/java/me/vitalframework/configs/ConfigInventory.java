package me.vitalframework.configs;

import lombok.NonNull;
import me.vitalframework.configs.annotation.Property;
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
public class ConfigInventory {
    @Property(InventoryType.class)
    public InventoryType type;

    @Property(ConfigItemStack.class)
    public ConfigItemStack[] contents;

    @NonNull
    public static ConfigInventory of(@NonNull Inventory inventory) {
        final ConfigInventory configInventory = new ConfigInventory();

        configInventory.type = inventory.getType();
        configInventory.contents = Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .map(ConfigItemStack::of)
                .toArray(ConfigItemStack[]::new);

        return configInventory;
    }

    @NonNull
    public Inventory toInventory(@NonNull InventoryHolder holder) {
        final Inventory inventory = Bukkit.createInventory(holder, type);

        inventory.setContents(Arrays.stream(contents)
                .map(ConfigItemStack::toItemStack)
                .toArray(ItemStack[]::new));

        return inventory;
    }
}
