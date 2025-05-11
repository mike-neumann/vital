package me.vitalframework.inventories

import me.vitalframework.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.springframework.stereotype.Component

@Component
class VitalInventoryListener(plugin: SpigotPlugin, val inventories: List<VitalInventory>) : VitalListener.Spigot(plugin) {
    @SpigotEventHandler
    fun onPlayerClickInInventory(e: InventoryClickEvent) {
        val player = e.whoClicked as SpigotPlayer
        val vitalInventory = inventories.firstOrNull { it.hasInventoryOpen(player) }

        if (e.clickedInventory == null) {
            val previousInventory = vitalInventory?.previousInventories?.get(player.uniqueId)

            if (vitalInventory != null && previousInventory != null) {
                vitalInventory.close(player)
                previousInventory.open(player)
            }
            return
        }

        if (vitalInventory == null || e.currentItem == null) return

        vitalInventory.click(e)
        e.isCancelled = true
    }

    @SpigotEventHandler
    fun onPlayerCloseInventory(e: InventoryCloseEvent) {
        val player = e.player as SpigotPlayer
        val vitalInventory = inventories.firstOrNull { it.hasInventoryOpen(player) }
        vitalInventory?.close(player)
    }
}