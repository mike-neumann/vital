package dev.vitalframework.holograms

import dev.vitalframework.SpigotEventHandler
import dev.vitalframework.VitalListener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Internal listener; Hides all holograms from other players using [VitalHologramService.hideOtherPerPlayerHolograms].
 */
class VitalPerPlayerHologramListener(
    private val vitalHologramService: VitalHologramService,
) : VitalListener.Spigot() {
    @SpigotEventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        vitalHologramService.hideOtherPerPlayerHolograms(e.player)
    }
}
