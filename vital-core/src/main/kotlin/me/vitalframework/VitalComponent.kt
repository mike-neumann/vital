package me.vitalframework

import java.util.*

/**
 * Interface representing a component that can be registered on a repository.
 */
interface VitalComponent {
    val uniqueId: UUID
    val name: String

    /**
     * Called when this component is registered on a repository.
     * Implement this method to perform any required initialization when the component is registered.
     */
    fun onRegistered()

    /**
     * Called when this component is unregistered from a repository.
     * Implement this method to perform any cleanup or finalization when the component is unregistered.
     */
    fun onUnregistered()
}