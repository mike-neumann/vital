package me.vitalframework.players

import me.vitalframework.VitalRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class VitalPlayerRepository : VitalRepository<VitalPlayer<UUID>, UUID>()