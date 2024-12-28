package me.vitalframework.players

import me.vitalframework.VitalRepository
import org.springframework.stereotype.Repository

@Repository
class VitalPlayerRepository : VitalRepository<VitalPlayer<*>>()