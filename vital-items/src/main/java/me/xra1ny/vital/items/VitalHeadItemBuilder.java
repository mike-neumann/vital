package me.xra1ny.vital.items;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

/**
 * Builder utility class designed for convenient creation of head item stacks.
 *
 * @author xRa1ny
 */
public class VitalHeadItemBuilder extends VitalItemBuilder {
    private OfflinePlayer owningPlayer;

    @Override
    public VitalHeadItemBuilder name(@Nullable String name) {
        super.name(name);

        return this;
    }

    @Override
    public VitalHeadItemBuilder type(Material type) {
        super.type(type);

        return this;
    }

    @Override
    public VitalHeadItemBuilder lore(@NonNull List<String> lore) {
        super.lore(lore);

        return this;
    }

    @Override
    public VitalHeadItemBuilder lore(@NonNull String lore) {
        super.lore(lore);

        return this;
    }

    @Override
    public VitalHeadItemBuilder itemFlags(@NonNull List<ItemFlag> itemFlagList) {
        super.itemFlags(itemFlagList);

        return this;
    }

    @Override
    public VitalHeadItemBuilder itemFlag(@NonNull ItemFlag itemFlag) {
        super.itemFlag(itemFlag);

        return this;
    }

    @Override
    public VitalHeadItemBuilder enchantments(@NonNull Map<Enchantment, Integer> enchantmentLevelMap) {
        super.enchantments(enchantmentLevelMap);

        return this;
    }

    @Override
    public VitalHeadItemBuilder enchantment(@NonNull Enchantment enchantment, int enchantmentLevel) {
        super.enchantment(enchantment, enchantmentLevel);

        return this;
    }

    @Override
    public VitalItemBuilder enchanted(boolean enchanted) {
        super.enchanted(enchanted);

        return this;
    }

    @Override
    public VitalHeadItemBuilder amount(int amount) {
        super.amount(amount);

        return this;
    }

    @Override
    public VitalHeadItemBuilder unbreakable(boolean unbreakable) {
        super.unbreakable(unbreakable);

        return this;
    }


    @Override
    public <Z> VitalHeadItemBuilder namespacedKey(@NonNull String key, @NonNull PersistentDataType<?, Z> persistentDataType, @NonNull Z value) {
        super.namespacedKey(key, persistentDataType, value);

        return this;
    }

    @Override
    public <Z> VitalHeadItemBuilder namespacedKey(@NonNull NamespacedKey namespacedKey, @NonNull PersistentDataType<?, Z> persistentDataType, @NonNull Z value) {
        super.namespacedKey(namespacedKey, persistentDataType, value);

        return this;
    }

    @Override
    public @NonNull <Z> ItemStack build() {
        final ItemStack itemStack = super.build();
        final SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        skullMeta.setOwningPlayer(owningPlayer);
        itemStack.setItemMeta(skullMeta);

        return itemStack;
    }

    /**
     * Sets the owning player of this head builder configuration.
     *
     * @param owningPlayer The owning player.
     * @return This builder config.
     */
    public VitalHeadItemBuilder owningPlayer(@Nullable OfflinePlayer owningPlayer) {
        this.owningPlayer = owningPlayer;

        return this;
    }
}