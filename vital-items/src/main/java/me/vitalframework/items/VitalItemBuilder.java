package me.vitalframework.items;

import lombok.NonNull;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * A builder class for creating ItemStack objects with custom attributes.
 * This class provides a fluent builder pattern for creating items with names, lore, enchantments, and more.
 */
public class VitalItemBuilder {
    private final List<String> lore = new ArrayList<>();
    private final List<ItemFlag> itemFlagList = new ArrayList<>();
    private final Map<Enchantment, Integer> enchantmentLevelMap = new HashMap<>();
    private final Map<NamespacedKey, Map.Entry<PersistentDataType<?, ?>, ?>> namespacedKeyMap = new HashMap<>();
    private String name;
    private Material type = Material.COBBLESTONE;
    private int amount = 1;
    private boolean unbreakable;

    /**
     * Define the name for this {@link ItemStack}.
     */
    public VitalItemBuilder name(String name) {
        this.name = name;

        return this;
    }

    /**
     * Define the type for this item.
     */
    public VitalItemBuilder type(Material type) {
        this.type = type;

        return this;
    }

    /**
     * Define the lore for this item.
     */
    public VitalItemBuilder lore(@NonNull List<String> lore) {
        this.lore.addAll(lore);

        return this;
    }

    /**
     * Add ONE lore LINE for this item.
     */
    public VitalItemBuilder lore(@NonNull String lore) {
        this.lore.add(lore);

        return this;
    }

    /**
     * Define the item flags for this item.
     */
    public VitalItemBuilder itemFlags(@NonNull List<ItemFlag> itemFlagList) {
        this.itemFlagList.addAll(itemFlagList);

        return this;
    }

    /**
     * Add ONE item flag for this item.
     */
    public VitalItemBuilder itemFlag(@NonNull ItemFlag itemFlag) {
        itemFlagList.add(itemFlag);

        return this;
    }

    /**
     * Define the enchantments for this item.
     */
    public VitalItemBuilder enchantments(@NonNull Map<Enchantment, Integer> enchantmentLevelMap) {
        this.enchantmentLevelMap.putAll(enchantmentLevelMap);

        return this;
    }

    /**
     * Add one enchantment and its level for this item.
     */
    public VitalItemBuilder enchantment(@NonNull Enchantment enchantment, int enchantmentLevel) {
        enchantmentLevelMap.put(enchantment, enchantmentLevel);

        return this;
    }

    /**
     * Adds an enchantment if true.
     */
    public VitalItemBuilder enchanted(boolean enchanted) {
        if (enchanted) {
            enchantment(Enchantment.LUCK, 1);
        }

        return this;
    }

    /**
     * Define the amount for this item.
     */
    public VitalItemBuilder amount(int amount) {
        this.amount = amount;

        return this;
    }

    /**
     * Define if this item is unbreakable or not.
     */
    public VitalItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;

        return this;
    }

    /**
     * Add one namespaced key for this item.
     */
    public <Z> VitalItemBuilder namespacedKey(@NonNull String key, @NonNull PersistentDataType<?, Z> persistentDataType, @NonNull Z value) {
        final var namespacedKey = new NamespacedKey("vital", key);

        namespacedKeyMap.put(namespacedKey, Map.entry(persistentDataType, value));

        return this;
    }

    /**
     * Add one namespaced key for this item.
     */
    public <Z> VitalItemBuilder namespacedKey(@NonNull NamespacedKey namespacedKey, @NonNull PersistentDataType<?, Z> persistentDataType, @NonNull Z value) {
        namespacedKeyMap.put(namespacedKey, Map.entry(persistentDataType, value));

        return this;
    }

    /**
     * Converts the builder's configuration into an ItemStack.
     */
    @NonNull
    public <Z> ItemStack build() {
        // Create ItemStack and ItemMeta
        final var item = new ItemStack(type, amount);

        if (type != Material.AIR) {
            final var meta = item.getItemMeta();
            final var persistentDataContainer = meta.getPersistentDataContainer();

            persistentDataContainer.set(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());

            if (name != null) {
                if (!name.isBlank()) {
                    meta.setDisplayName(
                            LegacyComponentSerializer.legacySection().serialize(
                                    MiniMessage.miniMessage().deserialize("<reset><white><name>",
                                                    Placeholder.parsed("name", name))
                                            .decoration(TextDecoration.ITALIC, false)
                            )
                    );
                }
            }

            // Set Enchantments if set
            if (!enchantmentLevelMap.isEmpty()) {
                for (var entrySet : enchantmentLevelMap.entrySet()) {
                    meta.addEnchant(entrySet.getKey(), entrySet.getValue(), true);
                }
            }

            // Set ItemFlags if set, else use all
            if (!itemFlagList.isEmpty()) {
                for (var itemFlag : itemFlagList) {
                    meta.addItemFlags(itemFlag);
                }
            } else {
                for (var itemFlag : ItemFlag.values()) {
                    meta.addItemFlags(itemFlag);
                }
            }

            // Set Lore if set
            if (!lore.isEmpty()) {
                meta.setLore(lore.stream()
                        .map(l -> MiniMessage.miniMessage().deserialize(l).decoration(TextDecoration.ITALIC, false))
                        .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                        .toList());
            }

            meta.setUnbreakable(unbreakable);

            if (!namespacedKeyMap.isEmpty()) {
                for (var entry : namespacedKeyMap.entrySet()) {
                    final var namespacedKey = entry.getKey();
                    // noinspection unchecked
                    final var persistentDataType = (PersistentDataType<?, Z>) entry.getValue().getKey();
                    // noinspection unchecked
                    final var value = (Z) entry.getValue().getValue();

                    if (!persistentDataType.getComplexType().equals(value.getClass())) {
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