package me.vitalframework.minigames

import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotListener
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalEntity
import me.vitalframework.tasks.VitalCountdownTask
import me.vitalframework.tasks.VitalRepeatableTask
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
        // Register all event handlers for this instance.
        registerEventHandlers(this) { _, event ->
            // This event handler should only be invoked when the event originates from this instance world.
            getWorld(event)?.name == world.name
        }

        onRegister()
    }

    /**
     * Unregisters all registered event handlers from this state and then calls [onUnregister].
     */
    fun unregister() {
        HandlerList.unregisterAll(this)
        onUnregister()
    }

    /**
     * Called when this instance is registered on the global [VitalMinigameInstanceService] bean.
     * Can be used to perform custom logic during instance registration.
     */
    open fun onRegister() {}

    /**
     * Called when this instance is unregistered on the global [VitalMinigameInstanceService] bean.
     * Can be used to perform custom logic during instance unregistration.
     */
    open fun onUnregister() {}

    /**
     * Returns the world in which the specified event occurred, or null if it cannot be determined.
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
     * Registers all event handler methods of the provided state.
     * All registered event handlers will only be called, when the given state is active on this game instance.
     */
    private fun <T : SpigotListener> registerEventHandlers(
        instance: T,
        predicate: (T, Event) -> Boolean,
    ) {
        val methods =
            instance.javaClass.methods
                .filter { AnnotationUtils.isAnnotationDeclaredLocally(SpigotEventHandler::class.java, it.javaClass) }
                .filter { Void.TYPE.isAssignableFrom(it.returnType) }
                .filter { it.parameters.size == 1 }
                .filter { Event::class.java.isAssignableFrom(it.parameters[0].type) }

        for (method in methods) {
            val eventClass = method.parameters[0].type as Class<out Event>
            val annotation = AnnotationUtils.getAnnotation(method, SpigotEventHandler::class.java)!!

            Bukkit.getPluginManager().registerEvent(
                eventClass,
                instance,
                annotation.priority,
                { listener, event ->
                    // The current event class type must match with the specified method parameter type,
                    // or else we will run into type mismatch problems.
                    if (!eventClass.isAssignableFrom(event.javaClass)) {
                        return@registerEvent
                    }

                    // Filter out any event that does not pass for our defined predicate.
                    if (!predicate(listener as T, event)) {
                        return@registerEvent
                    }

                    // The event that occurred here, was fired in the same world as our instance.
                    // we can safely execute its event handler here to scope the event to this instance and its currently active state.
                    method(listener, event)
                },
                plugin,
            )
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
    }
}
