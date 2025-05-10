package me.vitalframework.holograms

import me.vitalframework.configs.VitalConfig
import org.bukkit.Location
import java.util.*

/**
 * Represents a global implementation of a vital hologram with string-based content.
 *
 * A VitalGlobalHologram is a specific type of [VitalHologram] that displays its content as a list of strings.
 * The hologram is identifiable by a unique [id] and is rendered in the game world at a specified [location].
 * It uses an underlying system of hidden entities, identified by [armorStandUniqueId] and [lineArmorStandUniqueIds],
 * to represent multiple lines of text.
 *
 * This class extends the functionality of [VitalHologram] by concretely defining the type of
 * content ([String]) that can be displayed and allows configuration through its properties.
 */
class VitalGlobalHologram() : VitalHologram<String>() {
    @VitalConfig.Property(UUID::class)
    override lateinit var id: UUID

    @VitalConfig.Property(String::class)
    override lateinit var name: String

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
        name: String,
        lines: List<String>,
        location: Location,
        armorStandUniqueId: UUID,
        lineArmorStandUniqueIds: List<UUID>,
    ) : this() {
        this.id = id
        this.name = name
        this.lines = lines
        this.location = location
        this.armorStandUniqueId = armorStandUniqueId
        this.lineArmorStandUniqueIds = lineArmorStandUniqueIds
    }
}