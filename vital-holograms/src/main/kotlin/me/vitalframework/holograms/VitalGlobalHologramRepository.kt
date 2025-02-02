package me.vitalframework.holograms

import me.vitalframework.VitalRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class VitalGlobalHologramRepository : VitalRepository<UUID, VitalGlobalHologram>() {
    fun get(name: String) = entities.find { it.name === name }
}