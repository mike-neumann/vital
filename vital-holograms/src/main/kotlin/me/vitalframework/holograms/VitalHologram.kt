package me.vitalframework.holograms

import me.vitalframework.VitalEntity
import org.bukkit.Location
import java.util.*

abstract class VitalHologram<T> internal constructor() : VitalEntity<UUID> {
    override lateinit var id: UUID
    open lateinit var name: String
    open lateinit var lines: List<T>
    open lateinit var location: Location
    open lateinit var armorStandUniqueId: UUID
    open lateinit var lineArmorStandUniqueIds: List<UUID>
}