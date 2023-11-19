package me.xra1ny.vital.items;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

/**
 * A builder class for creating ItemStack objects with custom attributes.
 * This class provides a fluent builder pattern for creating items with names, lore, enchantments, and more.
 *
 * @author xRa1ny
 */
@Getter(onMethod = @__(@NotNull))
public final class VitalItemStackBuilder {
    private String name;

    private Material type = Material.COBBLESTONE;

    private List<String> lore = new ArrayList<>();

    private List<ItemFlag> itemFlagList = new ArrayList<>();

    private Map<Enchantment, Integer> enchantmentLevelMap = new HashMap<>();

    private int amount = 1;

    private boolean unbreakable;

    private Map<NamespacedKey, Map.Entry<PersistentDataType<?, ?>, ?>> namespacedKeyMap = new HashMap<>();

    public VitalItemStackBuilder name(String name) {
        this.name = name;

        return this;
    }

    public VitalItemStackBuilder type(Material type) {
        this.type = type;

        return this;
    }

    public VitalItemStackBuilder lore(List<String> lore) {
        this.lore = lore;

        return this;
    }

    public VitalItemStackBuilder lore(String lore) {
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }

        this.lore.add(lore);

        return this;
    }

    public VitalItemStackBuilder itemFlags(List<ItemFlag> itemFlagList) {
        this.itemFlagList = itemFlagList;

        return this;
    }

    public VitalItemStackBuilder itemFlag(ItemFlag itemFlag) {
        if (this.itemFlagList == null) {
            this.itemFlagList = new ArrayList<>();
        }

        this.itemFlagList.add(itemFlag);

        return this;
    }

    public VitalItemStackBuilder enchantments(Map<Enchantment, Integer> enchantmentLevelMap) {
        this.enchantmentLevelMap = enchantmentLevelMap;

        return this;
    }

    public VitalItemStackBuilder enchantment(Enchantment enchantment, int enchantmentLevel) {
        if (this.enchantmentLevelMap == null) {
            this.enchantmentLevelMap = new HashMap<>();
        }

        this.enchantmentLevelMap.put(enchantment, enchantmentLevel);

        return this;
    }

    public VitalItemStackBuilder amount(int amount) {
        this.amount = amount;

        return this;
    }

    public VitalItemStackBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;

        return this;
    }

    public <Z> VitalItemStackBuilder namespacedKey(@NotNull String key, @NotNull PersistentDataType<?, Z> persistentDataType, @NotNull Z value) {
        final NamespacedKey namespacedKey = new NamespacedKey("vital", key);

        namespacedKeyMap.put(namespacedKey, Map.entry(persistentDataType, value));

        return this;
    }

    public <Z> VitalItemStackBuilder namespacedKey(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataType<?, Z> persistentDataType, @NotNull Z value) {
        namespacedKeyMap.put(namespacedKey, Map.entry(persistentDataType, value));

        return this;
    }

    /**
     * Converts the builder's configuration into an ItemStack.
     *
     * @return The constructed ItemStack.
     */
    @SuppressWarnings("deprecation")
    @NotNull
    public <Z> ItemStack build() {
        // Create ItemStack and ItemMeta
        final ItemStack item = new ItemStack((type != null ? type : Material.COBBLESTONE), amount);

        if (this.type != Material.AIR) {
            final ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(NamespacedKeys.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());

            if (this.name != null) {
                if (this.name.isBlank()) {
                    meta.setDisplayName(ChatColor.RESET.toString());
                } else {
                    meta.setDisplayName(this.name);
                }
            }

            // Set Enchantments if set
            if (enchantmentLevelMap != null && !enchantmentLevelMap.isEmpty()) {
                for (Entry<Enchantment, Integer> entrySet : enchantmentLevelMap.entrySet()) {
                    meta.addEnchant(entrySet.getKey(), entrySet.getValue(), true);
                }
            }

            // Set ItemFlags if set, else use all
            if (itemFlagList != null && !itemFlagList.isEmpty()) {
                for (ItemFlag itemFlag : itemFlagList) {
                    meta.addItemFlags(itemFlag);
                }
            } else {
                for (ItemFlag itemFlag : ItemFlag.values()) {
                    meta.addItemFlags(itemFlag);
                }
            }

            // Set Lore if set
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }

            meta.setUnbreakable(unbreakable);

            if(!namespacedKeyMap.isEmpty()) {
                final PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();

                for(Map.Entry<NamespacedKey, Map.Entry<PersistentDataType<?, ?>, ?>> entry : namespacedKeyMap.entrySet()) {
                    final NamespacedKey namespacedKey = entry.getKey();
                    final PersistentDataType<?, Z> persistentDataType = (PersistentDataType<?, Z>) entry.getValue().getKey();
                    final Z value = (Z) entry.getValue().getValue();

                    if(!persistentDataType.getComplexType().equals(value.getClass())) {
                        continue;
                    }

                    persistentDataContainer.set(namespacedKey, persistentDataType, value);
                }
            }

            // Set created ItemStack's ItemMeta
            item.setItemMeta(meta);
        }

        return item;
    }
}

