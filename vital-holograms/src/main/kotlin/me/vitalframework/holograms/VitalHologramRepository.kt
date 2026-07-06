package me.vitalframework.holograms

import me.vitalframework.VitalRepository
import java.util.UUID

/**
 * Global repository; stores all holograms created with [VitalHologramService].
 */
class VitalHologramRepository : VitalRepository<VitalHologram, UUID>()
