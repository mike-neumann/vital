package me.vitalframework.configs

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

/**
 * Wrapper class to store itemstack data in a config file.
 */
class VitalConfigItemStack {
    companion object {
        fun of(itemStack: ItemStack): VitalConfigItemStack {
            val itemMeta = itemStack.itemMeta
            val vitalConfigItemStack = VitalConfigItemStack()

            vitalConfigItemStack.type = itemStack.type

            if (itemMeta!!.displayName != null) {
                vitalConfigItemStack.displayName = itemMeta.displayName
            } else {
                vitalConfigItemStack.displayName = itemStack.type.name
            }

            vitalConfigItemStack.lore =
                if (!itemMeta.hasLore()) {
                    mutableListOf<String>()
                } else {
                    itemMeta.lore!!.toMutableList()
                }

            vitalConfigItemStack.enchantments =
                mutableMapOf(*itemMeta.enchants.entries.map { it.key.key.key to it.value }.toTypedArray())
            vitalConfigItemStack.itemFlags = itemMeta.itemFlags.toMutableList()

            return vitalConfigItemStack
        }
    }

    @VitalConfig.Property(Material::class)
    var type: Material? = null

    @VitalConfig.Property(String::class)
    var displayName: String? = null

    @VitalConfig.Property(String::class)
    var lore = mutableListOf<String>()

    @VitalConfig.Property(
        String::class, Int::class
    )
    var enchantments = mutableMapOf<String, Int>()

    @VitalConfig.Property(ItemFlag::class)
    var itemFlags = mutableListOf<ItemFlag>()

    fun toItemStack(): ItemStack {
        val itemStack = ItemStack(type!!)
        val itemMeta = itemStack.itemMeta

        itemMeta!!.setDisplayName(displayName)
        itemMeta.lore = lore
        enchantments.forEach { (key: String?, level: Int?) ->
            itemMeta.addEnchant(
                Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key!!))!!, level!!, true
            )
        }
        itemMeta.addItemFlags(*itemFlags.toTypedArray())

        itemStack.itemMeta = itemMeta

        return itemStack
    }
}