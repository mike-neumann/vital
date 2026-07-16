package me.vitalframework

import me.vitalframework.VitalCoreModule.Companion.logger

/**
 * A volatile in-memory repository to store temporary data in a Spring-like repository implementation.
 * It can store implementations of [VitalEntity] and the means to get, save and delete them.
 *
 * Useful for temporary data, that should be wiped when the server restarts.
 *
 * ```java
 * @Component
 * public class MyRepository extends VitalRepository<MyEntity> {
 *
 * }
 * ```
 *
 * DO NOT ANNOTATE YOUR IMPLEMENTATIONS WITH [org.springframework.stereotype.Repository].
 * THIS WILL BREAK INSTANCE INTERNALS.
 */
abstract class VitalRepository<T : VitalEntity<ID>, ID> {
    private val logger = logger()

    private val _entities = mutableMapOf<ID, T>()
    val entities: List<T>
        get() = _entities.values.toList()

    /**
     * Saves all given [entities] by calling [save] for each one.
     * If an empty list is provided, this function does nothing.
     */
    fun saveAll(entities: List<T>) {
        for (entity in entities) {
            save(entity)
        }
    }

    /**
     * Saves the given [entity] to this repository.
     * If an entity with the given [entity] id ([VitalEntity.id]) already exists in this repository,
     * it will be deleted via [delete] and then overridden
     */
    fun <X : T> save(entity: X): X {
        if (existsById(entity.id)) {
            delete(entity)
        }

        _entities[entity.id] = entity
        onSave(entity)
        return entity
    }

    fun existsById(id: ID) = _entities.containsKey(id)

    /**
     * Gets the first entity that is an instance of the given type and has the given [id].
     */
    inline fun <reified T : VitalEntity<ID>> findById(id: ID) = findById(T::class.java, id)

    // TODO: performance

    /**
     * Gets the first entity that is an instance of the given [type] and has the given [id].
     */
    fun <T : VitalEntity<ID>> findById(
        type: Class<T>,
        id: ID,
    ) = _entities
        .values
        .filterIsInstance(type)
        .find { it.id == id }

    /**
     * Gets all entities from this repository that are an instance of the given type.
     */
    inline fun <reified T : VitalEntity<ID>> findAll() = findAll(T::class.java)

    /**
     * Gets all entities from this repository that are an instance of the given [type].
     */
    fun <T : VitalEntity<ID>> findAll(type: Class<T>) = _entities.values.filterIsInstance(type)

    /**
     * Gets a random entity from this repository that matches the given [predicate].
     * If no entity was found, this function returns `null`.
     */
    @JvmOverloads
    fun getRandom(predicate: (T) -> Boolean = { true }) = _entities.values.filter(predicate).randomOrNull()

    /**
     * Deletes the given [entity] from this repository and calls the [onDelete] lifecycle.
     * If the given entity does not exists in this repository, this function does nothing.
     */
    fun delete(entity: T) {
        if (!_entities.containsKey(entity.id)) {
            return
        }

        _entities.remove(entity.id)
        onDelete(entity)
    }

    /**
     * Lifecycle function; called when the given [entity] is saved to this repository.
     */
    protected fun onSave(entity: T) {
        logger.debug("Lifecycle function 'onSave(T)' was not overridden for Vital repository '$this'.")
    }

    /**
     * Lifecycle function; called when the given [entity] is deleted from this repository.
     */
    protected fun onDelete(entity: T) {
        logger.debug("Lifecycle function 'onDelete(T)' was not overridden for Vital repository '$this'.")
    }
}
