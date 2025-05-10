package me.vitalframework.players

import me.vitalframework.VitalRepository
import org.springframework.stereotype.Component
import java.util.*

/**
 * Repository class for managing `VitalPlayer` entities in-memory.
 *
 * This class extends `VitalRepository` to provide a runtime storage solution
 * specifically tailored to `VitalPlayer` instances. It supports operations such as
 * saving, retrieving, deleting, and checking the existence of `VitalPlayer` entities.
 *
 * The entities managed by this repository adhere to the generic `VitalPlayer` type,
 * parameterized by their specific player type and identified by a `UUID`.
 *
 * This repository is designed to work seamlessly within the Vital framework and is
 * used by services such as `VitalPlayerService` to manage player-related logic.
 */
@Component
class VitalPlayerRepository : VitalRepository<VitalPlayer<*>, UUID>()