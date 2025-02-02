package me.vitalframework.items

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

open class VitalItemStackBuilder {
    var type = Material.COBBLESTONE
    var name: String? = null
    var amount = 1
    var unbreakable = false
    var lore = mutableListOf<String>()
    var itemFlags = mutableListOf<ItemFlag>()
    var enchantments = mutableMapOf<Enchantment, Int>()
}

inline fun itemBuilder(init: VitalItemStackBuilder.() -> Unit): ItemStack {
    val itemStackBuilder = VitalItemStackBuilder().apply { init() }

    return ItemStack(itemStackBuilder.type, itemStackBuilder.amount).apply {
        if (type == Material.AIR) {
            return this
        }
        // since we know we have an item which is not of type AIR, we now have a persistent data container
        itemMeta = itemMeta!!.apply {
            // each item MUST have a unique identifier, used in interactive items.
            persistentDataContainer.set(
                VitalNamespacedKey.ITEM_UUID,
                PersistentDataType.STRING,
                UUID.randomUUID().toString()
            )

            if (itemStackBuilder.name != null) {
                setDisplayName(
                    LegacyComponentSerializer.legacySection().serialize(
                        MiniMessage.miniMessage().deserialize("<reset><white>${itemStackBuilder.name}")
                            .decoration(TextDecoration.ITALIC, false)
                    )
                )
            }

            for ((enchantment, level) in itemStackBuilder.enchantments) {
                addEnchant(enchantment, level, true)
            }

            for (itemFlag in itemStackBuilder.itemFlags) {
                addItemFlags(itemFlag)
            }

            lore = itemStackBuilder.lore
                .map { MiniMessage.miniMessage().deserialize(it).decoration(TextDecoration.ITALIC, false) }
                .map { LegacyComponentSerializer.legacySection().serialize(it) }

            isUnbreakable = itemStackBuilder.unbreakable
        }
    }
}