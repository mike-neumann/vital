package me.vitalframework.items

import me.vitalframework.Listener
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotEventHandler
import me.vitalframework.VitalListener
import org.bukkit.event.player.PlayerInteractEvent

@RequiresSpigot
@Listener
class VitalItemListener(
    val itemService: VitalItemService,
) : VitalListener.Spigot() {
    @SpigotEventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        itemService.handleInteraction(e)
    }
}
