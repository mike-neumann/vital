package me.vitalframework.items

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

open class VitalItemStackBuilder {
    var type = Material.COBBLESTONE
    var name: String? = null
    var amount = 1
    var unbreakable = false
    var lore = mutableListOf<String>()
    var itemFlags = mutableListOf<ItemFlag>()
    var enchantments = mutableMapOf<Enchantment, Int>()
}