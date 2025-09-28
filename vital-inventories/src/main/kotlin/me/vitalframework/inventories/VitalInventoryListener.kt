package me.vitalframework.inventories

import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotPlayer
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalListener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.stereotype.Component

@RequiresSpigot
@Component
class VitalInventoryListener(
    plugin: SpigotPlugin,
    val inventories: List<VitalInventory>,
) : VitalListener.Spigot(plugin) {
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

        e.isCancelled = true
        vitalInventory.click(e)
    }

    @SpigotEventHandler
    fun onPlayerCloseInventory(e: InventoryCloseEvent) {
        val player = e.player as SpigotPlayer
        val vitalInventory = inventories.firstOrNull { it.hasInventoryOpen(player) }
        vitalInventory?.close(player)
    }

    @SpigotEventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        for (inventory in inventories) {
            inventory.close(e.player)
        }
    }
}
