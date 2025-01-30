package me.vitalframework

/**
 * Abstract class for managing a list of registered components.
 */
abstract class VitalRepository<ID, T : VitalEntity<ID>> {
    private val entities = mutableListOf<T>()

    /**
     * Saves the specified entity.
     */
    fun save(entity: T) {
        if (exists(entity)) {
            return
        }

        entities.add(entity)
        onSave(entity)
    }

    /**
     * Checks if the specified entity is saved on this repository.
     */
    fun exists(entity: T): Boolean = entities.contains(entity)

    /**
     * Checks if an entity is saved by the specified id.
     */
    fun exists(id: ID): Boolean = entities.any { it.id == id }

    /**
     * Gets all saved entities on this repository.
     */
    fun getAll(): List<T> = entities

    /**
     * Gets a saved entity by its id.
     */
    fun get(id: ID): T? = entities.find { it.id == id }

    /**
     * Gets a random entity, matching the given predicate.
     */
    @JvmOverloads
    fun getRandom(predicate: (T) -> Boolean = { true }): T? = entities
            .filter(predicate)
            .randomOrNull()


    /**
     * Deletes the specified entity from this repository.
     */
    fun delete(entity: T) {
        if (!exists(entity)) {
            return
        }

        entities.remove(entity)
        onDelete(entity)
    }

    /**
     * Called when an entity has been saved.
     */
    protected open fun onSave(entity: T) {

    }

    /**
     * Called when an entity has been deleted.
     */
    protected open fun onDelete(entity: T) {

    }
}