package me.vitalframework.items

import me.vitalframework.VitalSubModule
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Component
import java.util.*

@Component("vital-items")
class VitalItemsSubModule : VitalSubModule()

fun itemBuilder(init: VitalItemStackBuilder.() -> Unit): ItemStack {
    val itemStackBuilder = VitalItemStackBuilder()

    itemStackBuilder.init()

    val item = ItemStack(itemStackBuilder.type, itemStackBuilder.amount)

    if (item.type == Material.AIR) {
        return item
    }

    // since we know we have an item which is not of type AIR, we now have a persistent data container
    val meta = item.itemMeta!!
    val persistentDataContainer = meta.persistentDataContainer

    // each item MUST have a unique identifier, used in interactive items.
    persistentDataContainer.set(VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING, UUID.randomUUID().toString())

    if (itemStackBuilder.name != null) {
        meta.setDisplayName(
            LegacyComponentSerializer.legacySection().serialize(
                MiniMessage.miniMessage().deserialize("<reset><white>${itemStackBuilder.name}")
                        .decoration(TextDecoration.ITALIC, false)
            )
        )
    }

    itemStackBuilder.enchantments.forEach { (enchantment, level) ->
        meta.addEnchant(enchantment, level, true)
    }

    itemStackBuilder.itemFlags.forEach {
        meta.addItemFlags(it)
    }

    meta.lore = itemStackBuilder.lore
        .map { MiniMessage.miniMessage().deserialize(it).decoration(TextDecoration.ITALIC, false) }
        .map { LegacyComponentSerializer.legacySection().serialize(it) }

    meta.isUnbreakable = itemStackBuilder.unbreakable
    item.itemMeta = meta

    return item
}

fun headBuilder(init: VitalHeadItemStackBuilder.() -> Unit): ItemStack {
    val headItemStackBuilder = VitalHeadItemStackBuilder()

    headItemStackBuilder.init()

    val item = itemBuilder {
        type = Material.PLAYER_HEAD
        name = headItemStackBuilder.name
        amount = headItemStackBuilder.amount
        unbreakable = headItemStackBuilder.unbreakable
        lore = headItemStackBuilder.lore
        itemFlags = headItemStackBuilder.itemFlags
        enchantments = headItemStackBuilder.enchantments
    }
    val meta = item.itemMeta as SkullMeta

    meta.owningPlayer = headItemStackBuilder.owningPlayer
    item.itemMeta = meta

    return item
}
