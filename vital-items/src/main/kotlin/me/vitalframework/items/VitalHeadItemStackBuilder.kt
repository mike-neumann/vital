package me.vitalframework.items

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class VitalHeadItemStackBuilder : VitalItemStackBuilder() {
    var owningPlayer: OfflinePlayer? = null
}

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
    }.apply { itemMeta = (itemMeta as SkullMeta).apply { owningPlayer = headItemStackBuilder.owningPlayer } }
}