package me.vitalframework.holograms

import me.vitalframework.configs.VitalConfig
import org.bukkit.Location
import java.util.UUID

/**
 * Represents a per-player implementation of [VitalHologram].
 * This hologram will only be visible to a single player when created using [VitalHologramService.createPerPlayerHologram].
 */
class VitalPerPlayerHologram() : VitalHologram<String>() {
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

    @VitalConfig.Property(UUID::class)
    lateinit var playerUniqueId: UUID

    constructor(
        id: UUID,
        lines: List<String>,
        location: Location,
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
        playerUniqueId: UUID,
    ) : this() {
        this.id = id
        this.lines = lines
        this.location = location
        this.armorStandUniqueId = armorStandUniqueId
        this.lineArmorStandUniqueIds = lineArmorStandUniqueIds
        this.playerUniqueId = playerUniqueId
    }
}
