package me.vitalframework.items

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.vitalframework.RequiresSpigot
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@RequiresSpigot
@Service
class VitalItemService(
    val items: List<VitalItem>,
) {
    fun handleInteraction(e: PlayerInteractEvent) {
        items.firstOrNull { it == e.item }?.handleInteraction(e)
    }

    @Scheduled(fixedRate = 50)
    suspend fun handleCooldown() =
        withContext(Dispatchers.IO) {
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
