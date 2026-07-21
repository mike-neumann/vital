package dev.vitalframework.holograms

import dev.vitalframework.VitalEntity
import org.bukkit.Location
import org.bukkit.Material
import java.util.UUID

/**
 * Base class for all holograms.
 * Please use the correct implementation for your use case: [VitalGlobalHologram], [VitalPerPlayerHologram].
 */
abstract class VitalHologram internal constructor() : VitalEntity<UUID> {
    override lateinit var id: UUID
    open lateinit var lines: List<Line>
    open lateinit var location: Location
    open lateinit var armorStandUniqueId: UUID
    open lateinit var lineArmorStandUniqueIds: List<UUID>

    data class Line(
        val text: String? = null,
        val material: Material? = null,
    )
}
