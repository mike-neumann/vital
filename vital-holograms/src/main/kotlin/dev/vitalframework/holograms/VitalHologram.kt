package dev.vitalframework.holograms

import dev.vitalframework.VitalEntity
import org.bukkit.Location
import org.bukkit.Material
import java.util.UUID

/**
 * Global hologram used to display information that is always the same for every player.
 * This hologram can't display any unique text for different players.
 * E.g., if you need to display language specific text, use [VitalPlayerHologram].
 */
open class VitalHologram(
    override var id: UUID,
    var lines: List<() -> Line>,
    var location: Location,
    var armorStandUniqueId: UUID,
    var lineArmorStandUniqueIds: List<UUID>,
) : VitalEntity<UUID> {
    data class Line(
        val text: String? = null,
        val material: Material? = null,
    )
}
