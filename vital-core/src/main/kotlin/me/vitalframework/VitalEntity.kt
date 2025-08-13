package me.vitalframework

/**
 * Represents a vital entity within the system, identified by a unique identifier of type [T].
 *
 * Classes implementing this interface must define a property for the entity's unique identifier.
 * This interface is designed to provide a common contract for entities managed within the vital framework.
 *
 * @param T The type of the identifier used by the entity.
 */
interface VitalEntity<T> {
    var id: T
}
