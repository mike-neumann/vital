package me.vitalframework.holograms

import me.vitalframework.VitalEntity
import org.bukkit.Location
import java.util.UUID

/**
 * Represents the basic structure of a hologram within the Vital framework.
 * Holograms can be created using [VitalHologramService].
 */
abstract class VitalHologram<T> internal constructor() : VitalEntity<UUID> {
    override lateinit var id: UUID
    open lateinit var lines: List<T>
    open lateinit var location: Location
    open lateinit var armorStandUniqueId: UUID
    open lateinit var lineArmorStandUniqueIds: List<UUID>
}
