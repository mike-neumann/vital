package me.vitalframework.items;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.RequiresAnnotation;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Used to create an item that can be interacted with.
 * This class provides a foundation for creating custom items with specific behaviors,
 * cooldowns, and interactions.
 */
@Getter
public abstract class VitalItem extends ItemStack implements RequiresAnnotation<VitalItem.Info> {
    @NonNull
    private final Map<Player, Integer> playerCooldownMap = new HashMap<>();

    private int initialCooldown = 0;

    public VitalItem() {
        final var info = getRequiredAnnotation();
        final var itemStack = new VitalItemBuilder()
                .type(info.type())
                .name(info.name())
                .amount(info.amount())
                .lore(List.of(info.lore()))
                .namespacedKey(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString())
                .itemFlags(List.of(info.itemFlags()))
                .unbreakable(info.unbreakable())
                .build();
        final var meta = itemStack.getItemMeta();

        if (info.enchanted()) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }

        // TODO: cannot set item meta, "delegate" is null
        setItemMeta(meta);
        setType(itemStack.getType());
        setAmount(itemStack.getAmount());
        this.initialCooldown = info.cooldown();
    }

    public VitalItem(@NonNull ItemStack itemStack, boolean enchanted) {
        final var meta = itemStack.getItemMeta();

        if (enchanted) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
        }

        // TODO: cannot set item meta, "delegate" is null
        setItemMeta(meta);
        setType(itemStack.getType());
        setAmount(itemStack.getAmount());
    }

    @Override
    public @NonNull Class<Info> requiredAnnotationType() {
        return Info.class;
    }

    public final void handleInteraction(@NonNull PlayerInteractEvent e) {
        if (!playerCooldownMap.containsKey(e.getPlayer())) {
            playerCooldownMap.put(e.getPlayer(), 0);
        }

        if (playerCooldownMap.get(e.getPlayer()) >= 1) {
            onCooldown(e);
            return;
        }

        final var action = e.getAction();

        switch (action) {
            case Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> onLeftClick(e);
            default -> onRightClick(e);
        }

        playerCooldownMap.put(e.getPlayer(), initialCooldown);
    }

    /**
     * Checks if this item is enchanted.
     */
    public final boolean isEnchanted() {
        return getItemMeta() == null || !getItemMeta().getEnchants().isEmpty();
    }

    /**
     * Gets the current cooldown of the given player.
     */
    public int getCooldown(@NonNull Player player) {
        return playerCooldownMap.getOrDefault(player, 0);
    }

    /**
     * Called when this item has been left-clicked.
     */
    public void onLeftClick(@NonNull PlayerInteractEvent e) {

    }

    /**
     * Called when this item has been right-clicked.
     */
    public void onRightClick(@NonNull PlayerInteractEvent e) {

    }

    /**
     * Called when this item has been left or right-clicked, but the cooldown has not yet expired.
     */
    public void onCooldown(@NonNull PlayerInteractEvent e) {

    }

    /**
     * Called when the cooldown of this item expires for the specified player.
     */
    public void onCooldownExpire(@NonNull Player player) {

    }

    /**
     * Called on every cooldown timer tick for the given player.
     */
    public void onCooldownTick(@NonNull Player player) {

    }

    @Override
    public final String toString() {
        return super.toString().replace("%s x %d"
                .formatted(getType(), getAmount()), getType() + " x 1");
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof ItemStack item)) {
            return false;
        }

        if (item.getItemMeta() == null) {
            return item.equals(this);
        }

        if (!item.getItemMeta().getPersistentDataContainer().has(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING)) {
            String toString = toString();

            return toString.equals(item.toString().replace("%s x %d"
                    .formatted(item.getType(), item.getAmount()), item.getType() + " x 1"));
        }

        final var uuid = UUID.fromString(getItemMeta().getPersistentDataContainer().get(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING));
        final var otherUuid = UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING));

        return uuid.equals(otherUuid);
    }

    /**
     * Annotation to provide information about an item that can be interacted with in the game.
     */
    @Component
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        /**
         * Defines the name of the custom item stack.
         */
        @NonNull
        String name();

        /**
         * Defines an array of lore lines for the custom item stack.
         * Lore lines provide additional information about the item.
         */
        @NonNull
        String[] lore() default {};

        /**
         * Defines the amount of items in the stack (stack size).
         */
        int amount() default 1;

        /**
         * Specifies the material or type of the custom item stack.
         */
        @NonNull
        Material type();

        /**
         * Defines an array of item flags that control specific display properties of the item stack.
         * Item flags can be used to hide attributes, enchantments, and more.
         */
        @NonNull
        ItemFlag[] itemFlags() default {};

        /**
         * Defines the cooldown time (in seconds) for the custom item stack's interaction.
         * The cooldown restricts how frequently the item can be used.
         */
        int cooldown() default 0;

        /**
         * Indicates whether the custom item stack should have enchantment visual effects.
         */
        boolean enchanted() default false;

        /**
         * Indicates whether the custom item stack should be unbreakable.
         */
        boolean unbreakable() default true;
    }
}