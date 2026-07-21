package dev.vitalframework.holograms

import dev.vitalframework.configs.VitalConfig
import org.bukkit.Location
import java.util.UUID

/**
 * Global hologram used to display player-specific data on a hologram for the given player.
 * Only the specified player will be able to see the created hologram when you create it using [VitalHologramService.createPerPlayerHologram].
 *
 * This hologram should be used if you want to display a hologram with unique content for each player.
 * Use this hologram if you also want to display language specific text.
 *
 * If you want to create a hologram that is always the same for each player (no language specifics), use [VitalGlobalHologram].
 */
class VitalPerPlayerHologram() : VitalHologram() {
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

    @VitalConfig.Property(UUID::class)
    lateinit var playerUniqueId: UUID

    constructor(
        id: UUID,
        lines: List<Line>,
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
