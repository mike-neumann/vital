package dev.vitalframework.holograms

import dev.vitalframework.VitalRepository
import java.util.UUID

/**
 * Global repository; stores all holograms created with [VitalHologramService].
 */
class VitalHologramRepository : VitalRepository<VitalHologram, UUID>()
