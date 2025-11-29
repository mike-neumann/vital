package me.vitalframework

import org.springframework.stereotype.Component

/**
 * An in-memory repository implementation that provides volatile data storage.
 *
 * This repository maintains data only during runtime and does not persist data
 * between application restarts. It is designed to work with entities that implement
 * [VitalEntity] interface.
 *
 * Important: This is a custom repository implementation and should not be confused
 * with Spring Boot repositories. Do not annotate implementations of this class
 * with Spring's @Repository annotation as it will interfere with internal
 * functionality.
 *
 * @param T The type of entity this repository manages, must implement [VitalEntity]
 */
@Component
abstract class VitalRepository<T : VitalEntity<ID>, ID> {
    private val _entities = mutableListOf<T>()
    val entities: List<T>
        get() = _entities

    fun save(entity: T): T {
        if (exists(entity)) delete(entity)
        _entities.add(entity)
        onSave(entity)
        return entity
    }

    fun exists(entity: T) = _entities.contains(entity)

    fun exists(id: ID) = _entities.any { it.id == id }

    fun get(id: ID) = _entities.find { it.id == id }

    @JvmOverloads
    fun getRandom(predicate: (T) -> Boolean = { true }) = _entities.filter(predicate).randomOrNull()

    fun delete(entity: T) {
        _entities.remove(entity)
        onDelete(entity)
    }

    protected fun onSave(entity: T) {}

    protected fun onDelete(entity: T) {}
}
