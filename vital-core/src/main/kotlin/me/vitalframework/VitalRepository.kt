package me.vitalframework

import java.util.*

/**
 * Abstract class for managing a list of registered components.
 */
abstract class VitalRepository<T : VitalComponent> {
    private val components = mutableListOf<T>()

    fun getAllComponents() = components.toList()

    @PublishedApi
    internal fun <X : T> getComponents(type: Class<X>) = components
        .filter { type.isAssignableFrom(it.javaClass) }
        .map { type.cast(it) }

    /**
     * Gets a list of all components matching the supplied type.
     */
    inline fun <reified X : T> getComponents() = getComponents(X::class.java)

    /**
     * Checks if a component is registered with the specified uuid.
     */
    fun isComponentRegistered(uniqueId: UUID) = getComponent(uniqueId) != null

    /**
     * Checks if a component is registered with the specified name.
     */
    fun isComponentRegistered(name: String) = getComponentByName(name) != null

    @PublishedApi
    internal fun <X : T> isComponentRegistered(type: Class<X>) = getComponent(type) != null

    /**
     * Checks if a component is registered with the specified type.
     */
    inline fun <reified X : T> isComponentRegistered() = isComponentRegistered(X::class.java)

    /**
     * Checks if the given component is registered on this repository.
     */
    fun isComponentRegistered(component: T): Boolean {
        return components.contains(component) || getComponent(component.uniqueId) != null
    }

    /**
     * Gets the component by the specified uuid.
     */
    fun getComponent(uniqueId: UUID) = components.find { uniqueId == it.uniqueId }

    /**
     * Gets the component by the specified name.
     */
    fun getComponentByName(name: String) = components.find { name == it.name }

    @PublishedApi
    internal fun <X : T> getComponent(type: Class<X>) = components
        .filter { type == it.javaClass }
        .map { type.cast(it) }
        .firstOrNull()

    /**
     * Gets the component by the specified class.
     */
    inline fun <reified X : T> getComponent() = getComponent(X::class.java)

    /**
     * Gets a random component, matching the given predicate registered on this repository.
     */
    @JvmOverloads
    fun getRandomComponent(predicate: (T) -> Boolean = { true }): T? {
        if (components.isEmpty()) {
            return null
        }

        val filteredVitalComponentList = components
            .filter(predicate)
            .toList()
        val randomIndex = Random().nextInt(filteredVitalComponentList.size)

        return filteredVitalComponentList[randomIndex]
    }

    /**
     * Registers the specified component.
     */
    fun registerComponent(component: T) {
        if (isComponentRegistered(component)) {
            return
        }

        components.add(component)
        onComponentRegistered(component)
        component.onRegistered()
    }

    /**
     * Unregisters the specified component.
     */
    fun unregisterComponent(component: T) {
        if (!isComponentRegistered(component)) {
            return
        }

        components.remove(component)
        component.onUnregistered()
        onComponentUnregistered(component)
    }

    fun unregisterAllComponents() {
        components.stream().toList().forEach { unregisterComponent(it) }
    }

    /**
     * Updates all components with the supplied ones
     */
    fun updateComponents(components: List<T>) {
        unregisterAllComponents()

        components.forEach { registerComponent(it) }
    }

    @PublishedApi
    internal fun <X : T> unregisterComponent(type: Class<X>) {
        getComponent(type)?.let { unregisterComponent(it) }
    }

    /**
     * Attempts to unregister a component by its specified class.
     */
    inline fun <reified X : T> unregisterComponent() {
        unregisterComponent(X::class.java)
    }

    /**
     * Called when the specified component is registered.
     */
    open fun onComponentRegistered(component: T) {

    }

    /**
     * Called when the specified component is unregistered.
     */
    open fun onComponentUnregistered(component: T) {

    }
}