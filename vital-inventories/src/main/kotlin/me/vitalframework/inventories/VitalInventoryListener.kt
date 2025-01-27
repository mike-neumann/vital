package me.vitalframework.inventories

import me.vitalframework.Vital.context
import me.vitalframework.VitalListener
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.stereotype.Component

/**
 * Listener for handling VitalInventoryMenu related events.
 */
@Component
class VitalInventoryListener(plugin: JavaPlugin) : VitalListener.Spigot(plugin) {
    @EventHandler
    fun onPlayerClickInInventory(e: InventoryClickEvent) {
        val clickedInventory = e.clickedInventory
        val player = e.whoClicked as Player
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

    @EventHandler
    fun onPlayerCloseInventory(e: InventoryCloseEvent) {
        val player = e.player as Player
        val vitalInventory = context.getBeansOfType(VitalInventory::class.java).values
            .firstOrNull { it.hasInventoryOpen(player) }

        vitalInventory?.close(player)
    }
}