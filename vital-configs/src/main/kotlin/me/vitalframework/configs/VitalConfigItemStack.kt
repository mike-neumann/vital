package me.vitalframework.configs

import org.bukkit.*
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class VitalConfigItemStack {
    companion object {
        fun of(itemStack: ItemStack): VitalConfigItemStack {
            val itemMeta = itemStack.itemMeta
            val vitalConfigItemStack = VitalConfigItemStack().apply {
                type = itemStack.type

                displayName = when {
                    itemMeta!!.displayName != null -> itemMeta.displayName
                    else -> itemStack.type.name
                }

                lore = when {
                    !itemMeta.hasLore() -> mutableListOf<String>()
                    else -> itemMeta.lore!!.toMutableList()
                }

                enchantments =
                    mutableMapOf(*itemMeta.enchants.entries.map { it.key.key.key to it.value }.toTypedArray())
                itemFlags = itemMeta.itemFlags.toMutableList()
            }

            return vitalConfigItemStack
        }
    }

    @VitalConfig.Property(Material::class)
    var type: Material? = null

    @VitalConfig.Property(String::class)
    var displayName: String? = null

    @VitalConfig.Property(String::class)
    var lore = mutableListOf<String>()

    @VitalConfig.Property(String::class, Int::class)
    var enchantments = mutableMapOf<String, Int>()

    @VitalConfig.Property(ItemFlag::class)
    var itemFlags = mutableListOf<ItemFlag>()

    fun toItemStack(): ItemStack {
        val itemStack = ItemStack(type!!)

        itemStack.itemMeta!!.apply {
            setDisplayName(displayName)
            lore = this@VitalConfigItemStack.lore

            for ((key, level) in enchantments) {
                addEnchant(Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key))!!, level, true)
            }

            addItemFlags(*itemFlags.toTypedArray())

            itemStack.itemMeta = this
        }

        return itemStack
    }
}