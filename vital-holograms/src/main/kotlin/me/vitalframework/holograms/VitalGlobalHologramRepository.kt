package me.vitalframework.holograms

import me.vitalframework.VitalRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class VitalGlobalHologramRepository : VitalRepository<VitalGlobalHologram, UUID>() {
    fun get(name: String) = entities.find { it.name === name }
}