package dev.vitalframework.holograms

import dev.vitalframework.SpigotPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID
import java.util.function.Function
import java.util.function.Supplier

/**
 * Global hologram used to display player-specific data on a hologram for the given player.
 * Only the specified player will be able to see the created hologram when you create it using [VitalHologramService.createPlayerHologram].
 *
 * This hologram should be used if you want to display a hologram with unique content for each player.
 * Use this hologram if you also want to display language specific text.
 *
 * If you want to create a hologram that is always the same for each player (no language specifics), use [VitalHologram].
 */
class VitalPlayerHologram(
    id: UUID,
    lines: List<Function<SpigotPlayer, Line>>,
    location: Location,
    armorStandUniqueId: UUID,
    lineArmorStandUniqueIds: List<UUID>,
    var playerUniqueId: UUID,
) : VitalHologram(
        id,
        lines.map { line ->
            Supplier { Bukkit.getPlayer(playerUniqueId)?.let { line.apply(it) } ?: Line() }
        },
        location,
        armorStandUniqueId,
        lineArmorStandUniqueIds,
    )
