package me.vitalframework

/**
 * Interface representing an entity that can be saved on a repository.
 */
interface VitalEntity<T> {
    var id: T
}