package me.xra1ny.vital.configs;

import lombok.NonNull;
import me.xra1ny.vital.configs.annotation.Property;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentWrapper;
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
    @Property(Material.class)
    public Material type;

    @Property(String.class)
    public String displayName;

    @Property(String.class)
    public List<String> lore;

    @Property({
            String.class,
            Integer.class
    })
    public Map<String, Integer> enchantments;

    @Property(ItemFlag.class)
    public List<ItemFlag> itemFlags;

    @NonNull
    public static ConfigItemStack of(@NonNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        final ConfigItemStack configItemStack = new ConfigItemStack();

        configItemStack.type = itemStack.getType();

        if (itemMeta.displayName() != null) {
            configItemStack.displayName = LegacyComponentSerializer.legacyAmpersand().serialize(itemMeta.displayName());
        } else {
            configItemStack.displayName = itemStack.getType().name();
        }

        configItemStack.lore = !itemMeta.hasLore() ? List.of() :
                itemMeta.lore().stream()
                        .map(LegacyComponentSerializer.legacyAmpersand()::serialize)
                        .toList();
        configItemStack.enchantments = Map.ofEntries(itemMeta.getEnchants().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().getKey().getKey(), entry.getValue()))
                .toArray(Map.Entry[]::new));
        configItemStack.itemFlags = itemMeta.getItemFlags().stream()
                .toList();

        return configItemStack;
    }

    @NonNull
    public ItemStack toItemStack() {
        final ItemStack itemStack = new ItemStack(type);
        final ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(displayName));
        itemMeta.lore(lore.stream()
                .map(LegacyComponentSerializer.legacyAmpersand()::deserialize)
                .toList());
        enchantments.forEach((key, level) -> itemMeta.addEnchant(new EnchantmentWrapper(key), level, true));
        itemMeta.addItemFlags(itemFlags.toArray(ItemFlag[]::new));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}