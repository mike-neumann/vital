package me.xra1ny.vital.inventories;

import lombok.Getter;
import lombok.NonNull;
import me.xra1ny.vital.core.AnnotatedVitalComponent;
import me.xra1ny.vital.inventories.annotation.VitalInventoryInfo;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class VitalInventory implements InventoryHolder, AnnotatedVitalComponent<VitalInventoryInfo> {
    @NonNull
    private Map<Integer, ItemStack> slotItemMap = new HashMap<>();

    @Getter
    @NonNull
    private final Map<Map.Entry<Player, ItemStack>, Consumer<InventoryClickEvent>> itemActionMap = new HashMap<>();

    @Nullable
    private ItemStack background;

    @Getter
    @Nullable
    private Inventory previousInventory;

    @Range(from = 0, to = 54)
    private final int size;

    @Getter
    @NonNull
    private final Inventory inventory;

    public VitalInventory() {
        final VitalInventoryInfo info = getRequiredAnnotation();

        final ItemStack backgroundItemStack = new ItemStack(info.background());
        final ItemMeta backgroundItemMeta = backgroundItemStack.getItemMeta();

        if (backgroundItemMeta != null) {
            backgroundItemMeta.displayName(null);
            backgroundItemStack.setItemMeta(backgroundItemMeta);
        }

        background = backgroundItemStack;
        size = info.size();
        inventory = Bukkit.createInventory(this, size, MiniMessage.miniMessage().deserialize(info.value()));
    }

    public VitalInventory(@NonNull Inventory previousInventory) {
        final VitalInventoryInfo info = getRequiredAnnotation();

        final ItemStack backgroundItemStack = new ItemStack(info.background());
        final ItemMeta backgroundItemMeta = backgroundItemStack.getItemMeta();

        if (backgroundItemMeta != null) {
            backgroundItemMeta.displayName(null);
            backgroundItemStack.setItemMeta(backgroundItemMeta);
        }

        background = backgroundItemStack;
        size = info.size();
        inventory = Bukkit.createInventory(this, size, MiniMessage.miniMessage().deserialize(info.value()));
        this.previousInventory = previousInventory;
    }

    protected void setItem(@Range(from = 0, to = 54) int slot, @Nullable ItemStack itemStack) {
        slotItemMap.put(slot, itemStack);
    }

    /**
     * Sets the given item to the specified slot while also binding an action to the given item and player in this inventory.
     *
     * @param slot The slot the item may occupy
     * @param itemStack The item itself.
     * @param player The player object for the click handler.
     * @param event The click handler itself.
     */
    protected void setItem(@Range(from = 0, to = 54) int slot, @NonNull ItemStack itemStack, @NonNull Player player, @NonNull Consumer<InventoryClickEvent> event) {
        setItem(slot, itemStack);
        onClick(player, itemStack, event);
    }

    protected void onOpen(@NonNull Player player) {

    }

    protected void onClose(@NonNull Player player) {

    }

    protected void onClick(@NonNull Player player, @NonNull ItemStack itemStack) {

    }

    protected void onClick(@NonNull Player player, @NonNull ItemStack itemStack, @NonNull Consumer<InventoryClickEvent> event) {
        itemActionMap.put(Map.entry(player, itemStack), event);
    }

    /**
     * Called when this inventory is updated.
     */
    public void onUpdate() {

    }

    /**
     * Called when this inventory is updated;
     *
     * @param player The player.
     * @apiNote This method is called for every player this inventory is updated.
     */
    public void onUpdate(@NonNull Player player) {

    }

    private void updateItems() {
        getInventory().clear();

        for(int i = 0; i < size; i++) {
            getInventory().setItem(i, background);
        }

        slotItemMap.forEach(getInventory()::setItem);
    }

    /**
     * Updates this inventory removing all items and resetting them for all players that have this inventory open.
     *
     * @see VitalInventory#onUpdate(Player)
     * @see VitalInventory#onUpdate()
     */
    public void update() {
        // first call developer onUpdate
        onUpdate();

        for (Player player : Bukkit.getOnlinePlayers()) {
            final InventoryHolder inventoryHolder = player.getOpenInventory().getTopInventory().getHolder();

            if (!(inventoryHolder instanceof VitalInventory vitalInventory) || !vitalInventory.equals(this)) {
                continue;
            }

            // update the inventory for the looping player.
            onUpdate(player);
        }

        // finally set all items accordingly
        updateItems();
    }

    @Override
    public Class<VitalInventoryInfo> requiredAnnotationType() {
        return VitalInventoryInfo.class;
    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onUnregistered() {

    }

    /**
     * Handles a player's click within this inventory menu.
     *
     * @param e The InventoryClickEvent.
     */
    public final void handleClick(@NonNull InventoryClickEvent e) {
        final Optional<ItemStack> optionalItemStack = Optional.ofNullable(e.getCurrentItem());

        if (optionalItemStack.isEmpty()) {
            return;
        }

        final ItemStack itemStack = optionalItemStack.get();
        final Material material = itemStack.getType();

        if (material.equals(Material.AIR) || itemStack.equals(background)) {
            return;
        }

        final Player player = (Player) e.getWhoClicked();

        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, .3f, 1f);

        final Optional<Consumer<InventoryClickEvent>> optionalItemAction = Optional.ofNullable(itemActionMap.getOrDefault(Map.entry(player, itemStack), null));

        optionalItemAction.ifPresentOrElse(itemAction -> itemAction.accept(e),
                () -> onClick(player, itemStack));
    }
}