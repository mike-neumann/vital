package me.vitalframework

import java.util.*

/**
 * Interface representing a component within the Vital-Framework.
 * Defines common methods and lifecycle events for Vital components.
 *
 * @author xRa1ny
 * @apiNote A component may be managed by its respective component repository ([VitalRepository])
 */
interface VitalComponent {
    val uniqueId: UUID
    val name: String

    /**
     * Called when this component is registered on a repository
     * Implement this method to perform any required initialization when the component is registered.
     */
    fun onRegistered()

    /**
     * Called when this component is unregistered from a repository
     * Implement this method to perform any cleanup or finalization when the component is unregistered.
     */
    fun onUnregistered()
}