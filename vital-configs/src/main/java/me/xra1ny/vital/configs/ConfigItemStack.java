package me.xra1ny.vital.configs;

import lombok.NonNull;
import me.xra1ny.vital.configs.annotation.Property;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
 * Wrapper class to store itemstack data in a config file.
 *
 * @author xRa1ny
 */
public class ConfigItemStack {
    /**
     * the type of this item
     */
    @Property(Material.class)
    public Material type;

    /**
     * the display name of this item
     */
    @Property(String.class)
    public String displayName;

    /**
     * all lore attached to this item's item meta
     */
    @Property(String.class)
    public List<String> lore;

    /**
     * all enchantments this item has
     */
    @Property({
            String.class,
            Integer.class
    })
    public Map<String, Integer> enchantments;

    /**
     * all item flags this item holds
     */
    @Property(ItemFlag.class)
    public List<ItemFlag> itemFlags;

    /**
     * converts a bukkit item stack into a config serializable one
     *
     * @param itemStack the bukkit item
     * @return the config item instance
     */
    @NonNull
    public static ConfigItemStack of(@NonNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        final ConfigItemStack configItemStack = new ConfigItemStack();

        configItemStack.type = itemStack.getType();

        if (itemMeta.getDisplayName() != null) {
            configItemStack.displayName = itemMeta.getDisplayName();
        } else {
            configItemStack.displayName = itemStack.getType().name();
        }

        configItemStack.lore = !itemMeta.hasLore() ? List.of() : itemMeta.getLore();
        configItemStack.enchantments = Map.ofEntries(itemMeta.getEnchants().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().getKey().getKey(), entry.getValue()))
                .toArray(Map.Entry[]::new));
        configItemStack.itemFlags = itemMeta.getItemFlags().stream()
                .toList();

        return configItemStack;
    }

    /**
     * converts this config item back into a bukkit one
     *
     * @return the bukkit item instance
     */
    @NonNull
    public ItemStack toItemStack() {
        final ItemStack itemStack = new ItemStack(type);
        final ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        enchantments.forEach((key, level) -> itemMeta.addEnchant(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key)), level, true));
        itemMeta.addItemFlags(itemFlags.toArray(ItemFlag[]::new));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}