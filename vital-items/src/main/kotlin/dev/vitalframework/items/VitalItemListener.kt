package dev.vitalframework.items

import dev.vitalframework.SpigotEventHandler
import dev.vitalframework.VitalListener
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Internal listener; handle the [PlayerInteractEvent] and delegates it to the correct [VitalItem].
 */
open class VitalItemListener(
    val vitalItemService: VitalItemService,
) : VitalListener.Spigot() {
    @SpigotEventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        vitalItemService.handleInteraction(e)
    }
}
