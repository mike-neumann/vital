package me.xra1ny.vital.items;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.xra1ny.vital.core.AnnotatedVitalComponent;
import me.xra1ny.vital.items.annotation.VitalItemStackInfo;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

/**
 * Used to create {@link ItemStack} that can be interacted with.
 * This class provides a foundation for creating custom items with specific behaviors,
 * cooldowns, and interactions.
 *
 * @author xRa1ny
 */
@SuppressWarnings("unused")
public abstract class VitalItemStack extends ItemStack implements AnnotatedVitalComponent<VitalItemStackInfo> {
    /**
     * The current Cooldown of this VitalItemStack.
     */
    @Getter
    @Setter
    private int currentCooldown = 0;

    /**
     * The Cooldown of this VitalItemStack.
     */
    @Getter
    private int cooldown = 0;

    /**
     * Creates a new VitalItemStack based on annotation-defined properties.
     *
     * @see VitalItemStackInfo
     */
    public VitalItemStack() {
        final VitalItemStackInfo info = getRequiredAnnotation();
        final ItemStack itemStack = new VitalItemStackBuilder()
                .type(info.type())
                .name(info.name())
                .amount(info.amount())
                .lore(List.of(info.lore()))
                .namespacedKey(NamespacedKeys.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString())
                .itemFlags(List.of(info.itemFlags()))
                .unbreakable(info.unbreakable())
                .build();
        final ItemMeta meta = itemStack.getItemMeta();

        if (info.enchanted()) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }

        setType(itemStack.getType());
        setAmount(itemStack.getAmount());
        setItemMeta(meta);
        this.cooldown = info.cooldown();
    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onUnregistered() {

    }

    /**
     * Creates a new VitalItemStack based on an existing ItemStack.
     *
     * @param itemStack The base ItemStack.
     * @param enchanted Whether to add enchantments.
     */
    public VitalItemStack(@NonNull ItemStack itemStack, boolean enchanted) {
        final ItemMeta meta = itemStack.getItemMeta();

        if (enchanted) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }

        setType(itemStack.getType());
        setAmount(itemStack.getAmount());
        setItemMeta(meta);
    }

    @Override
    public Class<VitalItemStackInfo> requiredAnnotationType() {
        return VitalItemStackInfo.class;
    }

    /**
     * Called when this item has been left-clicked.
     *
     * @param e The player interact event.
     */
    public void onLeftClick(@NonNull PlayerInteractEvent e) {

    }

    /**
     * Called when this item has been right-clicked.
     *
     * @param e The player interact event.
     */
    public void onRightClick(@NonNull PlayerInteractEvent e) {

    }

    /**
     * Called when this item has been left or right-clicked, but the cooldown has not yet expired.
     *
     * @param e The player interact event.
     */
    public void onCooldown(@NonNull PlayerInteractEvent e) {

    }

    /**
     * Called when the cooldown of this item expires for the specified player.
     *
     * @param player The player.
     */
    public void onCooldownExpire(@NonNull Player player) {

    }

    /**
     * Handles player interaction with this item, considering cool-downs.
     *
     * @param e The player interact event.
     */
    public final void handleInteraction(@NonNull PlayerInteractEvent e) {
        if (currentCooldown >= 1) {
            onCooldown(e);
            return;
        }

        final Action action = e.getAction();

        if (action.isLeftClick()) {
            onLeftClick(e);
        } else {
            onRightClick(e);
        }

        currentCooldown = cooldown;
    }

    @Override
    public final String toString() {
        return super.toString().replace(getType() + " x " + getAmount(), getType() + " x 1");
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof ItemStack item)) {
            return false;
        }

        if (!item.getItemMeta().getPersistentDataContainer().has(NamespacedKeys.ITEM_UUID, PersistentDataType.STRING)) {
            String toString = toString();

            return toString.equals(item.toString().replace(item.getType() + " x " + item.getAmount(), item.getType() + " x 1"));
        }

        final UUID uuid = UUID.fromString(getItemMeta().getPersistentDataContainer().get(NamespacedKeys.ITEM_UUID, PersistentDataType.STRING));
        final UUID otherUuid = UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(NamespacedKeys.ITEM_UUID, PersistentDataType.STRING));

        return uuid.equals(otherUuid);
    }

    /**
     * Checks if this item is enchanted.
     *
     * @return true if the item is enchanted, false otherwise.
     */
    public final boolean isEnchanted() {
        return !getItemMeta().getEnchants().isEmpty();
    }
}

