package dev.vitalframework.holograms

import dev.vitalframework.configs.VitalConfig
import org.bukkit.Location
import java.util.UUID

/**
 * Global hologram used to display information that is always the same for every player.
 * This hologram can't display any unique text for different players.
 * E.g., if you need to display language specific text, use [VitalPerPlayerHologram].
 */
class VitalGlobalHologram() : VitalHologram() {
    @VitalConfig.Property(UUID::class)
    override lateinit var id: UUID

    @VitalConfig.Property(String::class)
    override lateinit var lines: List<Line>

    @VitalConfig.Property(Location::class)
    override lateinit var location: Location

    @VitalConfig.Property(UUID::class)
    override lateinit var armorStandUniqueId: UUID

    @VitalConfig.Property(UUID::class)
    override lateinit var lineArmorStandUniqueIds: List<UUID>

    constructor(
        id: UUID,
        lines: List<Line>,
        location: Location,
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
    ) : this() {
        this.id = id
        this.lines = lines
        this.location = location
        this.armorStandUniqueId = armorStandUniqueId
        this.lineArmorStandUniqueIds = lineArmorStandUniqueIds
    }
}
