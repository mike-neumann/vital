package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital.context
import org.springframework.stereotype.Service

/**
 * The main vital inventory service for registering inventories.
 */
@Service
class VitalInventoryService {
    fun openInventory(player: SpigotPlayer, vitalInventoryClass: Class<VitalInventory>) {
        val vitalInventory: VitalInventory = context.getBean(vitalInventoryClass)

        vitalInventory.open(player)
    }

    fun getInventories(vitalInventoryClass: Class<VitalInventory>): List<VitalInventory> =
        try {
            context.getBeansOfType(vitalInventoryClass).values.toList()
        } catch (e: Exception) {
            listOf()
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