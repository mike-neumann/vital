package me.vitalframework.inventories

import me.vitalframework.SpigotPlayer
import me.vitalframework.Vital
import org.springframework.stereotype.Service

@Service
class VitalInventoryService {
    fun openInventory(player: SpigotPlayer, vitalInventoryClass: Class<VitalInventory>) {
        val vitalInventory: VitalInventory = Vital.context.getBean(vitalInventoryClass)

        vitalInventory.open(player)
    }

    fun getInventories(vitalInventoryClass: Class<VitalInventory>) = try {
        Vital.context.getBeansOfType(vitalInventoryClass).values.toList()
    } catch (e: Exception) {
        emptyList()
    }

    fun getInventories() = getInventories(VitalInventory::class.java)

    fun <T : VitalInventory> getInventory(vitalInventoryClass: Class<T>) = try {
        Vital.context.getBean(vitalInventoryClass)
    } catch (e: Exception) {
        null
    }

    fun updateInventories() {
        for (inventory in getInventories()) {
            inventory.update()
        }
    }
}