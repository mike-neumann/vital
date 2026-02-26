package me.vitalframework.players

import me.vitalframework.VitalRepository
import java.util.UUID

/**
 * The global repository for in-memory data management for [VitalPlayer] instances.
 * When using "vital-players", a custom [VitalPlayer] instance defined by the "vital.players.vital-player-class-name" is created and stored here.
 * This repository can be used to retrieve all managed [VitalPlayer] instances.
 *
 * Manual mutation of this repository is discouraged.
 */
class VitalPlayerRepository : VitalRepository<VitalPlayer<*>, UUID>()
