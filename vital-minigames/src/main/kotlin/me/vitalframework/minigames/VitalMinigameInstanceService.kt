package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import net.kyori.adventure.util.TriState
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import java.util.UUID

/**
 * The global minigame instance manager to use when creating multiple minigames on the same game server.
 * Each minigame will run in its own capsulated instance, which can have n-amount of states.
 * A state may also have n-amount of event handlers mapped to it.
 * Each event handler will only be active when the state is also active.
 *
 * E.g. "StartingState" -> JoinEvent -> Event handler will only be active when the starting state is active.
 * Once you switch to a different state, like "IngameState", the event handlers from "StartingState" will no longer fire.
 */
open class VitalMinigameInstanceService(
    private val vitalMinigameInstanceRepository: VitalMinigameInstanceRepository,
    private val plugin: SpigotPlugin,
) {
    /**
     * Loads a new instance world by the given [templateWorldName].
     * This function will copy all world files from the [templateWorldName] to a new world that can be used for a new [VitalMinigameInstance], e.g. "[VitalMinigameInstance.id]_[templateWorldName]",
     * and will then immediately return.
     * An [action] function can be provided to perform an action on the newly loaded world.
     */
    @JvmOverloads
    fun loadTemplateWorld(
        templateWorldName: String,
        action: (World?) -> Unit = {},
    ) {
        Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            Runnable {
                // Locate the given template world and copy its content to its own world for the given instance.
                val templateWorldFile = Bukkit.getWorldContainer().resolve(templateWorldName)
                if (!templateWorldFile.exists()) {
                    throw IllegalArgumentException("Template world '$templateWorldName' not found.")
                }

                val instanceId = UUID.randomUUID()
                val instanceWorldFile = templateWorldFile.copyTo(Bukkit.getWorldContainer().resolve("${templateWorldName}_$instanceId"))

                val templateWorld = Bukkit.getWorld(templateWorldName)!!
                // Finally actually load the world.
                Bukkit.getScheduler().runTask(
                    plugin,
                    Runnable {
                        action(
                            Bukkit.createWorld(
                                WorldCreator(instanceWorldFile.name)
                                    .copy(templateWorld)
                                    .keepSpawnLoaded(TriState.FALSE)
                                    .generateStructures(false),
                            ),
                        )
                    },
                )
            },
        )
    }

    /**
     * Loads the world by the given [name] with settings to reduce lag-spikes on the server's main-thread.
     */
    fun loadWorld(name: String): World? =
        Bukkit.createWorld(
            WorldCreator(name)
                .keepSpawnLoaded(TriState.FALSE)
                .generateStructures(false),
        )

    /**
     * Registers a new game instance on this service.
     * An instance may perform custom logic during registration via [VitalMinigameInstance.onRegister].
     * Additionally, an [action] function can be provided to perform an action right after instance registration, but before [VitalMinigameInstance.onRegister] is called.
     *
     * Fails if an instance with the same id as [instance] already exists.
     */
    @JvmOverloads
    fun <T : VitalMinigameInstance> registerInstance(
        instance: T,
        action: (T) -> Unit = {},
    ) {
        if (vitalMinigameInstanceRepository.existsById(instance.id)) {
            throw IllegalStateException()
        }

        vitalMinigameInstanceRepository.save(instance)
        action(instance)
        instance.register()
    }

    /**
     * Unregisters an existing game instance by its [id], fails if no instance with the given [id] exists.
     * Once unregistered, the instance's world will be unloaded and then deleted.
     * Additionally, an [beforeDeleteAction] function can be provided to perform an action right after instance unregistration, but before [VitalMinigameInstance.onUnregister] is called and before the world is deleted.
     * An [afterDeleteAction] function can also be provided to perform an action right after the world unloaded and deleted from the file system.
     */
    @JvmOverloads
    inline fun <reified T : VitalMinigameInstance> unregisterInstance(
        id: UUID,
        noinline beforeDeleteAction: (T) -> Unit = {},
        noinline afterDeleteAction: (T) -> Unit = {},
    ) {
        unregisterInstance(T::class.java, id, beforeDeleteAction, afterDeleteAction)
    }

    /**
     * Unregisters an existing game instance by its [id], fails if no instance with the given [id] exists.
     * Once unregistered, the instance's world will be unloaded and then deleted.
     * Additionally, an [beforeDeleteAction] function can be provided to perform an action right after instance unregistration, but before [VitalMinigameInstance.onUnregister] is called and before the world is deleted.
     * An [afterDeleteAction] function can also be provided to perform an action right after the world unloaded and deleted from the file system.
     */
    @PublishedApi
    internal fun <T : VitalMinigameInstance> unregisterInstance(
        type: Class<T>,
        id: UUID,
        beforeDeleteAction: (T) -> Unit = {},
        afterDeleteAction: (T) -> Unit = {},
    ) {
        if (!vitalMinigameInstanceRepository.existsById(id)) {
            throw IllegalArgumentException()
        }

        val instance = vitalMinigameInstanceRepository.findById(type, id)!!
        beforeDeleteAction(instance)
        unregisterInstance(instance)
    }

    /**
     * Unregisters an existing game instance.
     * An instance may perform custom logic during unregistration via [VitalMinigameInstance.onUnregister].
     * Additionally, an [beforeDeleteAction] function can be provided to perform an action right after instance unregistration, but before [VitalMinigameInstance.onUnregister] is called and before the world is deleted.
     * An [afterDeleteAction] function can also be provided to perform an action right after the world unloaded and deleted from the file system.
     */
    @JvmOverloads
    fun <T : VitalMinigameInstance> unregisterInstance(
        instance: T,
        beforeDeleteAction: (T) -> Unit = {},
        afterDeleteAction: (T) -> Unit = {},
    ) {
        vitalMinigameInstanceRepository.delete(instance)
        beforeDeleteAction(instance)
        instance.unregister()

        Bukkit.unloadWorld(instance.world, false)
        Bukkit.getScheduler().runTaskAsynchronously(
            plugin,
            Runnable {
                Bukkit.getWorldContainer().resolve(instance.world.name).deleteRecursively()
                afterDeleteAction(instance)
            },
        )
    }

    /**
     * Unregisters all existing game instances by calling [unregisterInstance] for every one.
     */
    @JvmOverloads
    fun unregisterAllInstances(
        beforeDeleteAction: (VitalMinigameInstance) -> Unit = {},
        afterDeleteAction: (VitalMinigameInstance) -> Unit = {},
    ) {
        for (instance in vitalMinigameInstanceRepository.findAll<VitalMinigameInstance>()) {
            unregisterInstance(instance, beforeDeleteAction, afterDeleteAction)
        }
    }
}
