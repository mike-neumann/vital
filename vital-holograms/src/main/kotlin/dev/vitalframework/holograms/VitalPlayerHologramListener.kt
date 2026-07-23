package dev.vitalframework.holograms

import dev.vitalframework.SpigotEventHandler
import dev.vitalframework.VitalListener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Internal listener; Hides all holograms from other players using [VitalHologramService.hideOtherPlayerHolograms].
 */
class VitalPlayerHologramListener(
    private val vitalHologramService: VitalHologramService,
) : VitalListener.Spigot() {
    @SpigotEventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        vitalHologramService.hideOtherPlayerHolograms(e.player)
    }
}
