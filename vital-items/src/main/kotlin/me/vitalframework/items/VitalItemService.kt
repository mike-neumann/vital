package me.vitalframework.items

import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class VitalItemService(val items: List<VitalItem>) {
    fun handleInteraction(e: PlayerInteractEvent) = items.firstOrNull { it == e.item }?.handleInteraction(e)

    @Scheduled(fixedRate = 50)
    fun handleCooldown() {
        for (item in items) {
            for ((uniqueId, _) in item.playerCooldown.filter { it.value > 0 }) {
                val player = Bukkit.getPlayer(uniqueId) ?: continue

                item.playerCooldown[uniqueId] = item.playerCooldown[uniqueId]!! - 50
                item.onCooldownTick(player)

                if (item.playerCooldown[uniqueId]!! <= 0) {
                    item.onCooldownExpire(player)
                }
            }
        }
    }
}