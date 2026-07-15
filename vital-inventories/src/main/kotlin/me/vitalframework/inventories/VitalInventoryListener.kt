package me.vitalframework.inventories

import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotPlayer
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalListener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Internal listener; processes and delegates inventory related Events to the correct [VitalInventory].
 */
class VitalInventoryListener(
    val inventories: List<VitalInventory>,
) : VitalListener.Spigot() {
    private val logger = logger()

    @SpigotEventHandler
    fun onPlayerClickInInventory(e: InventoryClickEvent) {
        val player = e.whoClicked as SpigotPlayer
        val vitalInventory = inventories.firstOrNull { it.hasInventoryOpen(player) }
        val loggingContext = "Context: player '${player.name}', inventory '${e.clickedInventory}', Vital inventory '$vitalInventory'."

        logger.debug("Player has clicked in an inventory. $loggingContext")

        if (e.clickedInventory == null) {
            logger.debug("Player clicked outside of an inventory view. $loggingContext")
            val previousInventory = vitalInventory?.previousInventories?.get(player.uniqueId)

            if (vitalInventory != null && previousInventory != null) {
                logger.debug("Will attempt to close previous Vital inventory. $loggingContext")
                vitalInventory.close(player)
                previousInventory.open(player)
            }
            return
        }

        if (vitalInventory == null || e.currentItem == null) return

        logger.debug("Delegating click event to Vital inventory. $loggingContext")
        e.isCancelled = true
        vitalInventory.click(e)
    }

    @SpigotEventHandler
    fun onPlayerCloseInventory(e: InventoryCloseEvent) {
        val player = e.player as SpigotPlayer
        val vitalInventory = inventories.firstOrNull { it.hasInventoryOpen(player) }
        val loggingContext = "Contxt: player '${player.name}', Vital inventory '$vitalInventory'."
        logger.debug("Will delegate inventory close event to Vital inventory if found. $loggingContext")
        vitalInventory?.close(player)
    }

    @SpigotEventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val loggingContext = "Context: player '${e.player.name}'."
        logger.debug("Player quit the server, will close all currently open Vital inventories. $loggingContext")
        for (inventory in inventories) {
            logger.debug("Closing Vital inventory '$inventory'. $loggingContext")
            inventory.close(e.player)
        }
    }
}
