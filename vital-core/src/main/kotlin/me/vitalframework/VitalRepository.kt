package me.vitalframework

import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

/**
 * Repository class, not to be confused with the spring boot repository.
 * This class provides volatile data storage.
 *
 * DO NOT ANNOTATE IMPLEMENTATIONS WITH [Repository]
 * AS THIS WILL BREAK INTERNAL FUNCTIONALITY.
 */
@Component
abstract class VitalRepository<T : VitalEntity<ID>, ID> {
    private val _entities = mutableListOf<T>()
    val entities: List<T> get() = _entities

    fun save(entity: T) = let {
        if (exists(entity)) delete(entity)
        entity.also {
            _entities.add(entity)
            onSave(entity)
        }
    }

    fun exists(entity: T) = _entities.contains(entity)
    fun exists(id: ID) = _entities.any { it.id == id }
    fun get(id: ID) = _entities.find { it.id == id }

    @JvmOverloads
    fun getRandom(predicate: (T) -> Boolean = { true }) = _entities.filter(predicate).randomOrNull()

    fun delete(entity: T) = run { _entities.remove(entity).also { onDelete(entity) } }

    protected open fun onSave(entity: T) {}
    protected open fun onDelete(entity: T) {}
}