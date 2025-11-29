package me.vitalframework.holograms

import me.vitalframework.configs.VitalConfig
import org.bukkit.Location
import java.util.UUID

/**
 * Represents a global implementation of [VitalHologram] with string-based content.
 * This hologram is visible to all players, when created using [VitalHologramService.createGlobalHologram].
 */
class VitalGlobalHologram() : VitalHologram<String>() {
    @VitalConfig.Property(UUID::class)
    override lateinit var id: UUID

    @VitalConfig.Property(String::class)
    override lateinit var lines: List<String>

    @VitalConfig.Property(Location::class)
    override lateinit var location: Location

    @VitalConfig.Property(UUID::class)
    override lateinit var armorStandUniqueId: UUID

    @VitalConfig.Property(UUID::class)
    override lateinit var lineArmorStandUniqueIds: List<UUID>

    constructor(
        id: UUID,
        lines: List<String>,
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
