package me.vitalframework.inventories

import me.vitalframework.Vital.context
import org.bukkit.entity.Player
import org.springframework.stereotype.Service

/**
 * The main vital inventory service for registering inventories.
 */
@Service
class VitalInventoryService {
    fun openInventory(player: Player, vitalInventoryClass: Class<VitalInventory>) {
        val vitalInventory: VitalInventory = context.getBean(vitalInventoryClass)

        vitalInventory.open(player)
    }

    fun getInventories(vitalInventoryClass: Class<VitalInventory>) =
        try {
            context.getBeansOfType(vitalInventoryClass).values
        } catch (e: Exception) {
            mutableListOf()
        }

    fun getInventories() = getInventories(VitalInventory::class.java)

    fun <T : VitalInventory> getInventory(vitalInventoryClass: Class<T>) =
        try {
            context.getBean(vitalInventoryClass)
        } catch (e: Exception) {
            null
        }

    fun updateInventories() {
        getInventories().forEach { it.update() }
    }
}