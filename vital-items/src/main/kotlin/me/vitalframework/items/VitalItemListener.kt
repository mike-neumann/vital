package me.vitalframework.items

import me.vitalframework.Listener
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalListener
import org.bukkit.event.player.PlayerInteractEvent

@RequiresSpigot
@Listener
class VitalItemListener(
    plugin: SpigotPlugin,
    val itemService: VitalItemService,
) : VitalListener.Spigot(plugin) {
    @SpigotEventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        itemService.handleInteraction(e)
    }
}
