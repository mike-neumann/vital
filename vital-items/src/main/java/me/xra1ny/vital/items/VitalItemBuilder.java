package me.xra1ny.vital.items;

import jakarta.annotation.Nullable;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.Map.Entry;

/**
 * A builder class for creating ItemStack objects with custom attributes.
 * This class provides a fluent builder pattern for creating items with names, lore, enchantments, and more.
 *
 * @author xRa1ny
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
     *
     * @param name The name.
     * @return This builder instance.
     */
    public VitalItemBuilder name(@Nullable String name) {
        this.name = name;

        return this;
    }

    /**
     * Define the type for this {@link ItemStack}.
     *
     * @param type The {@link Material}.
     * @return This builder instance.
     */
    public VitalItemBuilder type(Material type) {
        this.type = type;

        return this;
    }

    /**
     * Define the lore for this {@link ItemStack}.
     *
     * @param lore The lore {@link List}.
     * @return This builder instance.
     */
    public VitalItemBuilder lore(@NonNull List<String> lore) {
        this.lore.addAll(lore);

        return this;
    }

    /**
     * Add ONE lore LINE for this {@link ItemStack}.
     *
     * @param lore The lore line to add.
     * @return This builder instance.
     */
    public VitalItemBuilder lore(@NonNull String lore) {
        this.lore.add(lore);

        return this;
    }

    /**
     * Define the item flags for this {@link ItemStack}.
     *
     * @param itemFlagList The {@link List} of all {@link ItemFlag} instances.
     * @return This builder instance.
     */
    public VitalItemBuilder itemFlags(@NonNull List<ItemFlag> itemFlagList) {
        this.itemFlagList.addAll(itemFlagList);

        return this;
    }

    /**
     * Add ONE {@link ItemFlag} for this {@link ItemStack}.
     *
     * @param itemFlag The {@link ItemFlag} to add.
     * @return This builder instance.
     */
    public VitalItemBuilder itemFlag(@NonNull ItemFlag itemFlag) {
        itemFlagList.add(itemFlag);

        return this;
    }

    /**
     * Define the enchantments for this {@link ItemStack}.
     *
     * @param enchantmentLevelMap The {@link Map} of all {@link Enchantment} instances and their level.
     * @return This builder instance.
     */
    public VitalItemBuilder enchantments(@NonNull Map<Enchantment, Integer> enchantmentLevelMap) {
        this.enchantmentLevelMap.putAll(enchantmentLevelMap);

        return this;
    }

    /**
     * Add one {@link Enchantment} and its level for this {@link ItemStack}.
     *
     * @param enchantment      The {@link Enchantment}.
     * @param enchantmentLevel The enchantment level.
     * @return This builder instance.
     */
    public VitalItemBuilder enchantment(@NonNull Enchantment enchantment, int enchantmentLevel) {
        enchantmentLevelMap.put(enchantment, enchantmentLevel);

        return this;
    }

    /**
     * Adds an enchantment if true.
     *
     * @param enchanted If this item should be enchanted.
     * @return This builder instance.
     */
    public VitalItemBuilder enchanted(boolean enchanted) {
        if (enchanted) {
            enchantment(Enchantment.LUCK, 1);
        }

        return this;
    }

    /**
     * Define the amount for this {@link ItemStack}.
     *
     * @param amount The amount.
     * @return This builder instance.
     */
    public VitalItemBuilder amount(int amount) {
        this.amount = amount;

        return this;
    }

    /**
     * Define if this {@link ItemStack} is unbreakable or not.
     *
     * @param unbreakable True if the {@link ItemStack} should be unbreakable; false otherwise.
     * @return This builder instance.
     */
    public VitalItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;

        return this;
    }

    /**
     * Add one {@link NamespacedKey} for this {@link ItemStack}.
     *
     * @param key                The key.
     * @param persistentDataType The {@link PersistentDataType} of the value to register.
     * @param value              The value the {@link NamespacedKey} instance should hold.
     * @param <Z>                The type of {@link PersistentDataType} the value should be composed of.
     * @return This builder instance.
     */
    public <Z> VitalItemBuilder namespacedKey(@NonNull String key, @NonNull PersistentDataType<?, Z> persistentDataType, @NonNull Z value) {
        final NamespacedKey namespacedKey = new NamespacedKey("vital", key);

        namespacedKeyMap.put(namespacedKey, Map.entry(persistentDataType, value));

        return this;
    }

    /**
     * Add one {@link NamespacedKey} for this {@link ItemStack}.
     *
     * @param namespacedKey      The {@link NamespacedKey}.
     * @param persistentDataType The {@link PersistentDataType} of the value to register.
     * @param value              The value the {@link NamespacedKey} instance should hold.
     * @param <Z>                The type of {@link PersistentDataType} the value should be composed of.
     * @return This builder instance.
     */
    public <Z> VitalItemBuilder namespacedKey(@NonNull NamespacedKey namespacedKey, @NonNull PersistentDataType<?, Z> persistentDataType, @NonNull Z value) {
        namespacedKeyMap.put(namespacedKey, Map.entry(persistentDataType, value));

        return this;
    }

    /**
     * Converts the builder's configuration into an ItemStack.
     *
     * @param <Z> Placeholder for {@link PersistentDataType} IGNORE.
     * @return The constructed ItemStack.
     */
    @NonNull
    public <Z> ItemStack build() {
        // Create ItemStack and ItemMeta
        final ItemStack item = new ItemStack(type, amount);

        if (type != Material.AIR) {
            final ItemMeta meta = item.getItemMeta();
            final PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();

            persistentDataContainer.set(NamespacedKeys.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString());

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
                for (Entry<Enchantment, Integer> entrySet : enchantmentLevelMap.entrySet()) {
                    meta.addEnchant(entrySet.getKey(), entrySet.getValue(), true);
                }
            }

            // Set ItemFlags if set, else use all
            if (!itemFlagList.isEmpty()) {
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
                meta.setLore(lore.stream()
                        .map(l -> MiniMessage.miniMessage().deserialize(l).decoration(TextDecoration.ITALIC, false))
                        .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                        .toList());
            }

            meta.setUnbreakable(unbreakable);

            if (!namespacedKeyMap.isEmpty()) {
                for (Map.Entry<NamespacedKey, Map.Entry<PersistentDataType<?, ?>, ?>> entry : namespacedKeyMap.entrySet()) {
                    final NamespacedKey namespacedKey = entry.getKey();

                    // noinspection unchecked
                    final PersistentDataType<?, Z> persistentDataType = (PersistentDataType<?, Z>) entry.getValue().getKey();
                    // noinspection unchecked
                    final Z value = (Z) entry.getValue().getValue();

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