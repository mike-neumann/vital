package me.vitalframework.items

import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class VitalItemService(val items: List<VitalItem>) {
    fun handleInteraction(e: PlayerInteractEvent) {
        items.firstOrNull { it == e.item }?.handleInteraction(e)
    }

    @Scheduled(fixedRate = 50)
    fun handleCooldown() {
        for (item in items) {
            item.playerCooldown
                .filter { it.value > 0 }
                .forEach { (player, _) ->
                    item.playerCooldown[player] = item.playerCooldown[player]!! - 50

                    item.onCooldownTick(player)

                    if (item.playerCooldown[player]!! <= 0) {
                        item.onCooldownExpire(player)
                    }
                }
        }
    }
}