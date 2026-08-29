package dev.vitalframework.minigames

import dev.vitalframework.SpigotEventHandler
import dev.vitalframework.SpigotListener
import dev.vitalframework.SpigotPlugin
import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalEntity
import dev.vitalframework.tasks.VitalCountdownTask
import dev.vitalframework.tasks.VitalRepeatableTask
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.weather.WeatherEvent
import org.bukkit.event.world.WorldEvent
import org.springframework.core.annotation.AnnotationUtils
import java.util.UUID
import java.util.function.BiPredicate

/**
 * Defines a single game instance that can be hosted on the server.
 * Each instance can have n-amount of states, but only one active state at a given time.
 * Every state may have n-amount of event handlers, which will only fire when their state is currently active on an instance.
 * Every instance will be bound to its own world for proper instance capsulation.
 *
 * An instance in itself is also a listener and can receive game events independently on which state is currently active.
 *
 * Switching to a state will register all defined event handlers for the new state, and unregister all from the old one.
 */
open class VitalMinigameInstance(
    override var id: UUID = UUID.randomUUID(),
    val plugin: SpigotPlugin,
    val world: World,
) : VitalEntity<UUID>,
    SpigotListener {
    private val logger = logger()

    /**
     * Holds the currently active state of this instance.
     * All event handlers defined on this state are active while this state is active.
     * Once deactivated by switching to another state, all event handlers will be unregistered.
     */
    var state: VitalMinigameInstanceState<*>? = null
        private set

    /**
     * Registers all declared event handlers for this state and then calls [onRegister].
     */
    fun register() {
        try {
            logger.debug("Registering VitalMinigameInstance with id '{}'.", id)
            // Register all event handlers for this instance.
            registerEventHandlers(this) { _, event ->
                // This event handler should only be invoked when the event originates from this instance world.
                getWorld(event)?.name == world.name
            }

            logger.debug("Successfully registered event handlers for VitalMinigameInstance with id '{}'. Calling onRegister", id)
            onRegister()
            logger.debug("Call to onRegister for VitalMinigameInstance with id '{}' returned successfully.", id)
        } catch (e: Exception) {
            throw VitalMinigameInstanceException.Register(id, javaClass, e)
        }
    }

    /**
     * Unregisters all registered event handlers from this state and then calls [onUnregister].
     */
    fun unregister() {
        try {
            logger.debug("Unregistering VitalMinigameInstance with id '{}'.", id)
            HandlerList.unregisterAll(this)
            logger.debug("Successfully unregistered event handlers from VitalMinigameInstance with id '{}'. Calling onUnregister.", id)
            onUnregister()
            logger.debug("Call to onUnregister for VitalMinigameInstance with id '{}' returned successfully.", id)
        } catch (e: Exception) {
            throw VitalMinigameInstanceException.Unregister(id, javaClass, e)
        }
    }

    /**
     * Lifecycle function; called when this instance is registered on the global [VitalMinigameInstanceService] bean.
     * Can be used to perform custom logic during instance registration.
     */
    open fun onRegister() {
        logger.debug("Lifecycle function 'onRegister' was not overridden for Vital minigame instance '$this'.")
    }

    /**
     * Lifecycle function; called when this instance is unregistered on the global [VitalMinigameInstanceService] bean.
     * Can be used to perform custom logic during instance unregistration.
     */
    open fun onUnregister() {
        logger.debug("Lifecycle function 'onUnregister' was not overridden for Vital minigame instance '$this'.")
    }

    /**
     * Internal function; gets the world in which the specified event occurred, or null if it cannot be determined.
     */
    private fun getWorld(e: Event): World? =
        when (e) {
            is EntityEvent -> e.entity.world
            is PlayerEvent -> e.player.world
            is BlockEvent -> e.block.world
            is WorldEvent -> e.world
            is WeatherEvent -> e.world
            is InventoryEvent -> e.view.player.world
            else -> null
        }

    /**
     * Internal function; registers all event handler methods of the provided state.
     * All registered event handlers will only be called, when the given state is active on this game instance.
     */
    private fun <T : SpigotListener> registerEventHandlers(
        instance: T,
        predicate: BiPredicate<T, Event>,
    ) {
        try {
            logger.debug("Registering event handlers for VitalMinigameInstance with id '{}'.", instance)

            val methods =
                instance.javaClass.methods
                    .filter { AnnotationUtils.findAnnotation(it, SpigotEventHandler::class.java) != null }
                    .filter { Void.TYPE.isAssignableFrom(it.returnType) }
                    .filter { it.parameters.size == 1 }
                    .filter { Event::class.java.isAssignableFrom(it.parameters[0].type) }

            logger.debug("Found '{}' event handlers for VitalMinigameInstance with id '{}'.", methods.size, id)
            for (method in methods) {
                val eventClass = method.parameters[0].type as Class<out Event>
                val annotation = AnnotationUtils.findAnnotation(method, SpigotEventHandler::class.java)!!

                logger.debug("Registering event handler for event '{}' for VitalMinigameInstance with id '{}'.", method.name, id)
                Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    instance,
                    annotation.priority,
                    { listener, event ->
                        logger.debug("Caught event '{}' for listener '{}' for VitalMinigameInstance with id '{}'.", event, listener, id)

                        if (!plugin.isEnabled) {
                            logger.debug(
                                "Event '{}' for listener '{}' for VitalMinigameInstance with id '{}' can not be executed because plugin '{}' is disabled.",
                                eventClass,
                                listener,
                                id,
                                plugin.name,
                            )
                            return@registerEvent
                        }

                        // The current event class type must match with the specified method parameter type,
                        // or else we will run into type mismatch problems.
                        if (!eventClass.isAssignableFrom(event.javaClass)) {
                            logger.debug(
                                "Event '{}' for listener '{}' for VitalMinigameInstance with id '{}' is not assignable to '{}'. This event will be ignored.",
                                eventClass,
                                listener,
                                id,
                                event.javaClass,
                            )
                            return@registerEvent
                        }

                        logger.debug(
                            "Event '{}' for listener '{}' for VitalMinigameInstance with id '{}' has passed assignable check. Checking against predicate.",
                            event.javaClass,
                            listener,
                            id,
                        )

                        // Filter out any event that does not pass for our defined predicate.
                        if (!predicate.test(listener as T, event)) {
                            logger.debug(
                                "Predicate for event '{}' for listener '{}' for VitalMinigameInstance with id '{}' did not pass. This event will be ignored.",
                                event.javaClass,
                                listener,
                                id,
                            )
                            return@registerEvent
                        }

                        logger.debug(
                            "Event handler for event '{}' for listener '{}' for VitalMinigameInstance with id '{}' will be called.",
                            event.javaClass,
                            listener,
                            id,
                        )

                        try {
                            // The event that occurred here, was fired in the same world as our instance.
                            // we can safely execute its event handler here to scope the event to this instance and its currently active state.
                            method(listener, event)
                            logger.debug(
                                "Event handler for event '{}' for listener '{}' for VitalMinigameInstance with id '{}' returned successfully.",
                                event.javaClass,
                                listener,
                                id,
                            )
                        } catch (e: Exception) {
                            logger.error(
                                "Error while executing event handler '${method.name}' for event '${eventClass.simpleName}' in class '${instance.javaClass.simpleName}' and instance with id '$id' and class '${this@VitalMinigameInstance.javaClass.simpleName}'.",
                                e,
                            )
                        }
                    },
                    plugin,
                )
            }

            logger.debug("Successfully registered '{}' event handlers for VitalMinigameInstance with id '{}'.", methods.size, id)
        } catch (e: Exception) {
            throw VitalMinigameInstanceException.RegisterEventHandlers(instance.javaClass, id, this@VitalMinigameInstance.javaClass, e)
        }
    }

    /**
     * Checks if the given state is active by its class.
     */
    inline fun <reified T : VitalMinigameInstanceState<*>> isStateActive(): Boolean = isStateActive(T::class.java)

    /**
     * Checks if the given state type is currently active on this game instance.
     */
    @PublishedApi
    internal fun isStateActive(stateClass: Class<out VitalMinigameInstanceState<*>>): Boolean =
        if (state == null) false else stateClass.isAssignableFrom(state!!.javaClass)

    /**
     * Sets the state of this instance to the given state.
     *
     * When a state is switched, all event handlers of the previous state will be unregistered for the given instance world and will become unresponsive to game events.
     * Tge new state will take over and receive new game events.
     *
     * If the previous state was a [VitalCountdownTask] or [VitalRepeatableTask], their respective [VitalCountdownTask.stop] and [VitalRepeatableTask.stop] functions are called.
     * Upon previous state unregistration, the [VitalMinigameInstanceState.onDisable] functions is called, the new state's [VitalMinigameInstanceState.onEnable] function is called.
     */
    fun setState(state: VitalMinigameInstanceState<*>?) {
        try {
            if (this.state != null) {
                if (this.state is VitalCountdownTask<*, *, *>) {
                    (this.state as VitalCountdownTask<*, *, *>).stop()
                }

                if (this.state is VitalRepeatableTask<*, *, *>) {
                    (this.state as VitalRepeatableTask<*, *, *>).stop()
                }

                HandlerList.unregisterAll(this.state!!)
                this.state!!.onDisable()
            }

            if (state != null) {
                this.state = state
                registerEventHandlers(state) { listener, event ->
                    // This event handler should only be invoked when the state provided is currently active on this instance.
                    if (!isStateActive(listener.javaClass)) {
                        return@registerEventHandlers false
                    }

                    // This event handler should only be invoked when the event originates from this instance world.
                    if (getWorld(event)?.name != world.name) {
                        return@registerEventHandlers false
                    }

                    return@registerEventHandlers true
                }
                state.onEnable()
            }
        } catch (e: Exception) {
            logger.error(
                "Error while switching state from '${this.state?.javaClass?.simpleName}' to '${state?.javaClass?.simpleName}' for instance with id '$id' and class '${javaClass.simpleName}'.",
                e,
            )
        }
    }

    companion object {
        const val MARKER = ".vital-minigame-instance"
    }
}
