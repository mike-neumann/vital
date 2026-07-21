package dev.vitalframework.items

import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.tasks.VitalScheduled
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Internal service; manages the interaction and cooldowns for all [VitalItem] instances.
 */
open class VitalItemService(
    val vitalItems: List<VitalItem>,
) {
    private val logger = logger()

    fun handleInteraction(e: PlayerInteractEvent) {
        vitalItems.firstOrNull { it == e.item }?.handleInteraction(e)
    }

    @VitalScheduled(fixedDelay = 50)
    fun handleCooldown() {
        for (item in vitalItems) {
            for ((uniqueId, _) in item.playerCooldown.filter { it.value > 0 }) {
                val player = Bukkit.getPlayer(uniqueId) ?: continue

                item.playerCooldown[uniqueId] = item.playerCooldown[uniqueId]!! - 50
                // So the log doesn't get spammed with messages, we are not logging the tick lifecycle execution.
                item.onCooldownTick(player)

                if (item.playerCooldown[uniqueId]!! <= 0) {
                    logger.debug("Calling 'onCooldownExpire(Player)' for player '$player' and Vital item '$item'.")
                    item.onCooldownExpire(player)
                }
            }
        }
    }
}
