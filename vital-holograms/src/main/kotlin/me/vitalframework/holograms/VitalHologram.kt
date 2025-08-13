package me.vitalframework.holograms

import me.vitalframework.VitalEntity
import org.bukkit.Location
import java.util.UUID

/**
 * Represents an abstract base class for all vital hologram entities in the system.
 *
 * Holograms are dynamic entities that can display multiple lines of content (of type [T]) in a specific [location].
 * Each hologram is uniquely identified by a [UUID] and is associated with one or more hidden in-game entities
 * (such as armor stands) for rendering purposes, tracked via [armorStandUniqueId] and [lineArmorStandUniqueIds].
 *
 * This class provides the foundational structure for holograms, which can be extended to implement various types
 * of holograms with specific requirements.
 *
 * @param T The type of content that the hologram lines will display.
 */
abstract class VitalHologram<T> internal constructor() : VitalEntity<UUID> {
    override lateinit var id: UUID
    open lateinit var name: String
    open lateinit var lines: List<T>
    open lateinit var location: Location
    open lateinit var armorStandUniqueId: UUID
    open lateinit var lineArmorStandUniqueIds: List<UUID>
}
