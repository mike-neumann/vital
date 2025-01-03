package me.vitalframework.inventories;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.RequiresAnnotation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Range;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class VitalInventory implements RequiresAnnotation<VitalInventory.Info> {
    private final int size;

    @NonNull
    private final String name;

    @NonNull
    private final Map<Player, Inventory> playerInventories = new HashMap<>();

    @NonNull
    private final Map<Integer, ItemStack> items = new HashMap<>();

    @NonNull
    private final Map<Map.Entry<Player, Integer>, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    private final VitalInventory previousInventory;

    public VitalInventory(VitalInventory previousInventory) {
        final var info = getRequiredAnnotation();

        size = info.size();
        name = info.name();
        this.previousInventory = previousInventory;
    }

    @Override
    public final @NonNull Class<Info> requiredAnnotationType() {
        return Info.class;
    }

    public void setItem(int slot, ItemStack itemStack, @NonNull Player player, @NonNull Consumer<InventoryClickEvent> action) {
        items.put(slot, itemStack);
        actions.put(Map.entry(player, slot), action);
    }

    public void setItem(int slot, ItemStack itemStack, @NonNull Player player) {
        setItem(slot, itemStack, player, e -> {
        });
    }

    public boolean hasInventoryOpen(@NonNull Player player) {
        return playerInventories.containsKey(player);
    }

    public void update() {
        onUpdate();

        playerInventories.forEach((player, inventory) -> update(player));
    }

    public void update(@NonNull Player player) {
        final var inventory = playerInventories.get(player);

        onUpdate(player);

        items.forEach(inventory::setItem);
    }

    public void open(@NonNull Player player) {
        final var inventory = Bukkit.createInventory(player, size, name);

        playerInventories.put(player, inventory);

        onOpen(player);
        update(player);
        player.openInventory(inventory);
    }

    public void click(@NonNull InventoryClickEvent e) {
        final var action = actions.get(Map.entry(e.getWhoClicked(), e.getSlot()));

        if (action != null) {
            action.accept(e);
        }
    }

    public void close(@NonNull Player player) {
        playerInventories.remove(player);
        onClose(player);
    }

    /**
     * used for when this inventory is opened for any player
     */
    public void onOpen(@NonNull Player player) {

    }

    /**
     * used for when setting static items (non player information)
     */
    public void onUpdate() {

    }

    /**
     * used for when needing to set items that hold player specific information
     */
    public void onUpdate(@NonNull Player player) {

    }

    /**
     * used for when this inventory is closed for an opened player
     */
    public void onClose(@NonNull Player player) {

    }

    /**
     * Annotation used to provide information about an inventory.
     */
    @Component
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        /**
         * The title of this inventory menu.
         */
        @NonNull
        String name();

        /**
         * The size in slots of this inventory menu. Default is 9 (one row).
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
}