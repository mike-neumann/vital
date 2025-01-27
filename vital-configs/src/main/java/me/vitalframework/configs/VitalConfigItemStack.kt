package me.vitalframework.configs;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Wrapper class to store itemstack data in a config file.
 */
public class VitalConfigItemStack {
    /**
     * the type of this item
     */
    @VitalConfig.Property(Material.class)
    public Material type;

    /**
     * the display name of this item
     */
    @VitalConfig.Property(String.class)
    public String displayName;

    /**
     * all lore attached to this item's item meta
     */
    @VitalConfig.Property(String.class)
    public List<String> lore;

    /**
     * all enchantments this item has
     */
    @VitalConfig.Property({
            String.class,
            Integer.class
    })
    public Map<String, Integer> enchantments;

    /**
     * all item flags this item holds
     */
    @VitalConfig.Property(ItemFlag.class)
    public List<ItemFlag> itemFlags;

    /**
     * converts a bukkit item stack into a config serializable one
     *
     * @param itemStack the bukkit item
     * @return the config item instance
     */
    @NonNull
    public static VitalConfigItemStack of(@NonNull ItemStack itemStack) {
        final var itemMeta = itemStack.getItemMeta();
        final var vitalConfigItemStack = new VitalConfigItemStack();

        vitalConfigItemStack.type = itemStack.getType();

        if (itemMeta.getDisplayName() != null) {
            vitalConfigItemStack.displayName = itemMeta.getDisplayName();
        } else {
            vitalConfigItemStack.displayName = itemStack.getType().name();
        }

        vitalConfigItemStack.lore = !itemMeta.hasLore() ? List.of() : itemMeta.getLore();
        vitalConfigItemStack.enchantments = Map.ofEntries(itemMeta.getEnchants().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().getKey().getKey(), entry.getValue()))
                .toArray(Map.Entry[]::new));
        vitalConfigItemStack.itemFlags = itemMeta.getItemFlags().stream()
                .toList();

        return vitalConfigItemStack;
    }

    /**
     * converts this config item back into a bukkit one
     *
     * @return the bukkit item instance
     */
    @NonNull
    public ItemStack toItemStack() {
        final var itemStack = new ItemStack(type);
        final var itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        enchantments.forEach((key, level) -> itemMeta.addEnchant(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)), level, true));
        itemMeta.addItemFlags(itemFlags.toArray(ItemFlag[]::new));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}