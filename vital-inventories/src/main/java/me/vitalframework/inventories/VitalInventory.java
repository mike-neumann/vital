package me.vitalframework.inventories;

import lombok.Getter;
import me.vitalframework.RequiresAnnotation;
import me.vitalframework.inventories.annotation.VitalInventoryInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class VitalInventory implements RequiresAnnotation<VitalInventoryInfo> {
    private final int size;
    private final String name;
    private final Map<Player, Inventory> playerInventories = new HashMap<>();
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Map.Entry<Player, Integer>, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    @Getter
    private final VitalInventory previousInventory;

    public VitalInventory(@Nullable VitalInventory previousInventory) {
        final VitalInventoryInfo info = getRequiredAnnotation();

        size = info.size();
        name = info.name();
        this.previousInventory = previousInventory;
    }

    public void setItem(int slot, ItemStack itemStack, Player player, Consumer<InventoryClickEvent> action) {
        items.put(slot, itemStack);
        actions.put(Map.entry(player, slot), action);
    }

    public void setItem(int slot, ItemStack itemStack, Player player) {
        setItem(slot, itemStack, player, e -> {
        });
    }

//    public void setGlobalItem(int slot, ItemStack itemStack, Consumer<InventoryClickEvent> action) {
//        globalItems.put(slot, Map.entry(itemStack, action));
//    }
//
//    public void setGlobalItem(int slot, ItemStack itemStack) {
//        setGlobalItem(slot, itemStack, e -> {
//        });
//    }
//
//    public void setPlayerItem(int slot, ItemStack itemStack, Player player, Consumer<InventoryClickEvent> action) {
//        playerItems.put(Map.entry(player, slot), Map.entry(itemStack, action));
//    }
//
//    public void setPlayerItem(int slot, ItemStack itemStack, Player player) {
//        setPlayerItem(slot, itemStack, player, e -> {
//        });
//    }

    @Override
    public final Class<VitalInventoryInfo> requiredAnnotationType() {
        return VitalInventoryInfo.class;
    }

    public boolean hasInventoryOpen(Player player) {
        return playerInventories.containsKey(player);
    }

    public void update() {
        onUpdate();

        playerInventories.forEach((player, inventory) -> update(player));
    }

    public void update(Player player) {
        final Inventory inventory = playerInventories.get(player);

        onUpdate(player);

        items.forEach(inventory::setItem);
        //globalItems.forEach((slot, entry) -> inventory.setItem(slot, entry.getKey()));
        //playerItems.forEach((playerSlot, entry) -> inventory.setItem(playerSlot.getValue(), entry.getKey()));
    }

    public void open(Player player) {
        final Inventory inventory = Bukkit.createInventory(player, size, name);

        playerInventories.put(player, inventory);

        onOpen(player);
        update(player);
        player.openInventory(inventory);
    }

    public void click(InventoryClickEvent e) {
//        final Map.Entry<ItemStack, Consumer<InventoryClickEvent>> entry = globalItems.get(e.getSlot());
//
//        if (entry != null) {
//            final Consumer<InventoryClickEvent> action = entry.getValue();
//
//            action.accept(e);
//        }
//
//        final Map.Entry<ItemStack, Consumer<InventoryClickEvent>> playerEntry = playerItems.get(Map.entry((Player) e.getWhoClicked(), e.getSlot()));
//
//        if (playerEntry != null) {
//            final Consumer<InventoryClickEvent> action = playerEntry.getValue();
//
//            action.accept(e);
//        }
        final Consumer<InventoryClickEvent> action = actions.get(Map.entry(e.getWhoClicked(), e.getSlot()));

        if (action != null) {
            action.accept(e);
        }
    }

    public void close(Player player) {
        playerInventories.remove(player);
        onClose(player);
    }

    /**
     * used for when this inventory is opened for any player
     */
    public void onOpen(Player player) {

    }

    /**
     * used for when setting static items (non player information)
     */
    public void onUpdate() {

    }

    /**
     * used for when needing to set items that hold player specific information
     */
    public void onUpdate(Player player) {

    }

    /**
     * used for when this inventory is closed for an opened player
     */
    public void onClose(Player player) {

    }
}