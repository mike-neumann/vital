package me.vitalframework.inventories

import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotPlayer
import me.vitalframework.SpigotPlugin
import me.vitalframework.Vital.context
import me.vitalframework.VitalListener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.springframework.stereotype.Component

/**
 * Listener for handling VitalInventoryMenu related events.
 */
@Component
class VitalInventoryListener(plugin: SpigotPlugin) : VitalListener.Spigot(plugin) {
    @SpigotEventHandler
    fun onPlayerClickInInventory(e: InventoryClickEvent) {
        val clickedInventory = e.clickedInventory
        val player = e.whoClicked as SpigotPlayer
        val vitalInventory = context.getBeansOfType(VitalInventory::class.java).values
            .firstOrNull { it.hasInventoryOpen(player) }

        if (clickedInventory == null) {
            // TODO: navigate to previous menu, currently still buggy
//            if (vitalInventory != null && vitalInventory.getPreviousInventory() != null) {
//                vitalInventory.close(player);
//                vitalInventory.getPreviousInventory().open(player);
//            }

            return
        }

        if (vitalInventory == null || e.currentItem == null) {
            return
        }

        vitalInventory.click(e)
        e.isCancelled = true
    }

    @SpigotEventHandler
    fun onPlayerCloseInventory(e: InventoryCloseEvent) {
        val player = e.player as SpigotPlayer
        val vitalInventory = context.getBeansOfType(VitalInventory::class.java).values
            .firstOrNull { it.hasInventoryOpen(player) }

        vitalInventory?.close(player)
    }
}