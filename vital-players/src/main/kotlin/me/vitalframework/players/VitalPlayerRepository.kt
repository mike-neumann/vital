package me.vitalframework.players

import me.vitalframework.VitalRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class VitalPlayerRepository : VitalRepository<VitalPlayer<*>, UUID>()