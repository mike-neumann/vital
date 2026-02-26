package me.vitalframework.items

import me.vitalframework.SpigotEventHandler
import me.vitalframework.VitalListener
import org.bukkit.event.player.PlayerInteractEvent

open class VitalItemListener(
    val vitalItemService: VitalItemService,
) : VitalListener.Spigot() {
    @SpigotEventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        vitalItemService.handleInteraction(e)
    }
}
