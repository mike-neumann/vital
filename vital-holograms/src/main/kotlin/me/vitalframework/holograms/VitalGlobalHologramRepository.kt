package me.vitalframework.holograms

import me.vitalframework.RequiresSpigot
import me.vitalframework.VitalRepository
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * A repository for managing instances of [VitalGlobalHologram].
 *
 * This class extends [VitalRepository] to provide in-memory data storage and management
 * for [VitalGlobalHologram] instances, identified by their unique [UUID].
 * The repository allows querying holograms by their name using the [get] function.
 */
@RequiresSpigot
@Component
class VitalGlobalHologramRepository : VitalRepository<VitalGlobalHologram, UUID>() {
    fun get(name: String) = entities.find { it.name === name }
}
