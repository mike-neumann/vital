package me.vitalframework

/**
 * A volatile in-memory repository to store "temporary" data in a Spring-like repository implementation.
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
 */
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

    fun existsById(id: ID) = _entities.any { it.id == id }

    inline fun <reified T : VitalEntity<ID>> findById(id: ID) = findById(T::class.java, id)

    fun <T : VitalEntity<ID>> findById(
        type: Class<T>,
        id: ID,
    ) = _entities
        .filterIsInstance(type)
        .find { it.id == id }

    inline fun <reified T : VitalEntity<ID>> findAll() = findAll(T::class.java)

    fun <T : VitalEntity<ID>> findAll(type: Class<T>) = _entities.filterIsInstance(type)

    @JvmOverloads
    fun getRandom(predicate: (T) -> Boolean = { true }) = _entities.filter(predicate).randomOrNull()

    fun delete(entity: T) {
        _entities.remove(entity)
        onDelete(entity)
    }

    protected fun onSave(entity: T) {}

    protected fun onDelete(entity: T) {}
}
