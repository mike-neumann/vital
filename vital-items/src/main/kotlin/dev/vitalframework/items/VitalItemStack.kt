package dev.vitalframework.items

import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.function.Consumer
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

/**
 * Convenience-class to build [ItemStack] instances using the Builder-Pattern.
 * Check the function to view how to use it.
 */
open class VitalItemStack(
    private var itemUuid: UUID? = null,
) {
    private var type = Material.COBBLESTONE
    private var name: String? = null
    private var amount = 1
    private var unbreakable = false
    private var lore = arrayOf<String>()
    private var itemFlags = arrayOf<ItemFlag>()
    private var enchantments = mutableMapOf<Enchantment, Int>()
    private var afterInit: Consumer<ItemStack> = Consumer {}
    private var glint: Boolean? = null

    fun itemUuid(itemUuid: UUID) = apply { this.itemUuid = itemUuid }

    fun type(type: Material) = apply { this.type = type }

    fun name(name: String?) = apply { this.name = name }

    fun amount(amount: Int) = apply { this.amount = amount }

    fun unbreakable(unbreakable: Boolean) = apply { this.unbreakable = unbreakable }

    fun lore(lore: Array<String>) = apply { this.lore = lore }

    fun itemFlags(itemFlags: Array<ItemFlag>) = apply { this.itemFlags = itemFlags }

    fun enchantments(enchantments: Map<Enchantment, Int>) = apply { this.enchantments = enchantments.toMutableMap() }

    fun enchantment(
        enchantment: Enchantment,
        level: Int,
    ) = apply { this.enchantments[enchantment] = level }

    fun glint(glint: Boolean?) = apply { this.glint = glint }

    fun afterInit(afterInit: Consumer<ItemStack>) = apply { this.afterInit = afterInit }

    open fun build(): ItemStack {
        val itemStack = ItemStack(type, amount)
        if (type == Material.AIR) {
            return itemStack
        }

        // since we know we have an item which is not of type AIR, we now have a persistent data container
        val itemMeta = itemStack.itemMeta
        // each item MUST have a unique identifier, used in interactive items.
        itemMeta.persistentDataContainer[VitalNamespacedKey.ITEM_UUID, PersistentDataType.STRING] =
            itemUuid?.toString() ?: UUID.randomUUID().toString()

        if (name != null) {
            itemMeta.displayName(
                MiniMessage
                    .miniMessage()
                    .deserialize("<reset><white>$name")
                    .decoration(TextDecoration.ITALIC, false),
            )
        }

        for ((enchantment, level) in enchantments) {
            itemMeta.addEnchant(enchantment, level, true)
        }

        for (itemFlag in itemFlags) {
            itemMeta.addItemFlags(itemFlag)
        }

        itemMeta.lore(
            lore.map {
                MiniMessage
                    .miniMessage()
                    .deserialize(
                        it,
                    ).decoration(TextDecoration.ITALIC, false)
            },
        )

        if (glint != null) {
            itemMeta.setEnchantmentGlintOverride(glint)
        }

        itemMeta.isUnbreakable = unbreakable
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun builder(itemUuid: UUID? = null): VitalItemStack = VitalItemStack(itemUuid)
    }
}
