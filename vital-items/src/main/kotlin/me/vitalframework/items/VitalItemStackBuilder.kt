package me.vitalframework.items

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**
 * A builder class for creating and configuring item stacks with customizable properties.
 *
 * This class provides a flexible way to define and modify item stack attributes such as type, name,
 * lore, enchantments, and other metadata. It allows for precise customization of items within the
 * Vital framework or for general Bukkit/Spigot development.
 *
 * Properties defined in this class can be adjusted to match specific item requirements, enabling
 * developers to create items with unique visual or functional characteristics.
 *
 * ### Features:
 * - Ability to set the type of the item (default is `Material.COBBLESTONE`).
 * - Option to specify a custom display name for the item.
 * - Configurable stack size through the `amount` property.
 * - Option to make the item unbreakable.
 * - Support for adding lore (descriptive text) to the item.
 * - Management of `ItemFlag` properties to customize the item's appearance and behavior.
 * - Ability to add enchantments and specify their levels.
 *
 * This class serves as a base for configuring items in a modular and readable way.
 */
open class VitalItemStackBuilder {
    var type = Material.COBBLESTONE
    var name: String? = null
    var amount = 1
    var unbreakable = false
    var lore = mutableListOf<String>()
    var itemFlags = mutableListOf<ItemFlag>()
    var enchantments = mutableMapOf<Enchantment, Int>()
}

/**
 * Constructs an `ItemStack` using the provided initialization logic
 * defined within the `VitalItemStackBuilder`.
 *
 * @param init A lambda function with a `VitalItemStackBuilder` receiver,
 * allowing customization of the item stack properties (e.g., type, name,
 * lore, enchantments, etc.).
 * @return The constructed `ItemStack` instance with the specified
 * properties applied.
 */
inline fun itemBuilder(itemUuid: UUID? = null, init: VitalItemStackBuilder.() -> Unit): ItemStack {
    val itemStackBuilder = VitalItemStackBuilder().apply { init() }

    return ItemStack(itemStackBuilder.type, itemStackBuilder.amount).apply {
        if (type == Material.AIR) return this
        // since we know we have an item which is not of type AIR, we now have a persistent data container
        itemMeta = itemMeta!!.apply {
            // each item MUST have a unique identifier, used in interactive items.
            persistentDataContainer[VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING] =
                itemUuid?.toString() ?: UUID.randomUUID().toString()

            if (itemStackBuilder.name != null) {
                displayName(
                    MiniMessage.miniMessage().deserialize("<reset><white>${itemStackBuilder.name}")
                        .decoration(TextDecoration.ITALIC, false)
                )
            }

            for ((enchantment, level) in itemStackBuilder.enchantments) {
                addEnchant(enchantment, level, true)
            }

            for (itemFlag in itemStackBuilder.itemFlags) {
                addItemFlags(itemFlag)
            }

            lore(itemStackBuilder.lore.map { MiniMessage.miniMessage().deserialize(it).decoration(TextDecoration.ITALIC, false) })

            isUnbreakable = itemStackBuilder.unbreakable
        }
    }
}