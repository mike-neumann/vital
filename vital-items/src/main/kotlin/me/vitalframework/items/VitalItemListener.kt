package me.vitalframework.items

import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalListener
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.stereotype.Component

@Component
class VitalItemListener(
    plugin: SpigotPlugin,
    val itemService: VitalItemService
) : VitalListener.Spigot(plugin) {
    @SpigotEventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        itemService.handleInteraction(e)
    }
}