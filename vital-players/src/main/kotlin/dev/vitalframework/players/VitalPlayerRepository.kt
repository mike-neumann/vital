package dev.vitalframework.players

import dev.vitalframework.VitalRepository
import java.util.UUID

/**
 * Global repository; stores all instances of [VitalPlayer].
 * Use this class to retrieve your custom instances.
 *
 * It is ill-advised to modify the contents of this repository manually.
 * Vital will automatically create new instances of your configured [VitalPlayer] class when a player joins
 * and also automatically delete any instance when a player leaves.
 */
class VitalPlayerRepository : VitalRepository<VitalPlayer<*>, UUID>()
