package me.vitalframework.items

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

/**
 * A specialized builder class for creating player head items in Bukkit/Spigot.
 * Extends the functionality of [VitalItemStackBuilder] by adding support for setting
 * the owning player property on player heads.
 *
 * This builder can be used in conjunction with the `headBuilder` utility function to
 * simplify the construction of customized player head items.
 *
 * @property owningPlayer The [OfflinePlayer] whose player head will be represented by the resulting item.
 */
class VitalHeadItemStackBuilder : VitalItemStackBuilder() {
    var owningPlayer: OfflinePlayer? = null
}

/**
 * Builds an `ItemStack` of type `Material.PLAYER_HEAD` configured with properties
 * defined by the provided initialization block.
 *
 * @param init A lambda receiver used to configure properties of the `VitalHeadItemStackBuilder`.
 * @return A configured `ItemStack` of type `Material.PLAYER_HEAD` with properties and metadata
 *         (e.g., name, lore, enchantments, owning player) applied from the builder.
 */
inline fun headBuilder(init: VitalHeadItemStackBuilder.() -> Unit): ItemStack {
    val headItemStackBuilder = VitalHeadItemStackBuilder().apply { init() }

    return itemBuilder {
        type = Material.PLAYER_HEAD
        name = headItemStackBuilder.name
        amount = headItemStackBuilder.amount
        unbreakable = headItemStackBuilder.unbreakable
        lore = headItemStackBuilder.lore
        itemFlags = headItemStackBuilder.itemFlags
        enchantments = headItemStackBuilder.enchantments
    }.apply {
        itemMeta = (itemMeta as SkullMeta).apply { owningPlayer = headItemStackBuilder.owningPlayer }
    }
}