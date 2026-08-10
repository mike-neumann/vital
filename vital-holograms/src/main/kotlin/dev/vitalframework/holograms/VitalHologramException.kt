package dev.vitalframework.holograms

import org.bukkit.entity.ArmorStand
import java.util.UUID

/**
 * Internal exception; thrown during [VitalHologram] lifecycles.
 */
abstract class VitalHologramException(
    message: String,
) : RuntimeException(message) {
    /**
     * Internal exception; thrown when a [VitalHologram] is updated but a target line entity is not an [ArmorStand].
     */
    class InvalidEntity(
        hologramId: UUID,
        targetUniqueId: UUID,
    ) : VitalHologramException(
            "Cannot create / update hologram with id '$hologramId', entity with uuid '$targetUniqueId' is not a valid armor stand.",
        )
}
