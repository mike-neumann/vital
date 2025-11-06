package me.vitalframework.holograms

import me.vitalframework.Listener
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalListener
import org.bukkit.event.player.PlayerJoinEvent

@Listener
class VitalPerPlayerHologramListener(
    plugin: SpigotPlugin,
    private val vitalHologramService: VitalHologramService,
) : VitalListener.Spigot(plugin) {
    @SpigotEventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        vitalHologramService.hideOtherPerPlayerHolograms(e.player)
    }
}
