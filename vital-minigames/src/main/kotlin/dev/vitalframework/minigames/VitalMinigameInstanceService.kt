package dev.vitalframework.minigames

import dev.vitalframework.SpigotPlugin
import dev.vitalframework.VitalCoreModule.Companion.logger
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
    private val logger = logger()

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
        if (!plugin.isEnabled) {
            throw VitalMinigameInstanceException.LoadTemplateWorldPluginDisabled(templateWorldName, plugin.name)
        }

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
                val task = {
                    try {
                        action(
                            Bukkit.createWorld(
                                WorldCreator(instanceWorldFile.name)
                                    .copy(templateWorld)
                                    .keepSpawnLoaded(TriState.FALSE)
                                    .generateStructures(false),
                            ),
                        )
                    } catch (e: Exception) {
                        throw VitalMinigameInstanceException.ExecuteLoadTemplateWorldAction(templateWorldName, plugin.name, e)
                    }
                }
                if (plugin.isEnabled) {
                    // Finally actually load the world.
                    Bukkit.getScheduler().runTask(plugin, task)
                } else {
                    logger.warn(
                        "Plugin is disabled, cannot schedule task for loading template world '{}' for instance with id '{}', attempting to run task directly.",
                        templateWorldName,
                        instanceId,
                    )
                    task()
                    logger.info(
                        "Direct invocation of task for loading template world '{}' for instance with id '{}' successful.",
                        templateWorldName,
                        instanceId,
                    )
                }
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
     * Additionally, an [afterRegisterAction] function can be provided to perform an action right after instance registration, but before [VitalMinigameInstance.onRegister] is called.
     *
     * Fails if an instance with the same id as [instance] already exists.
     */
    @JvmOverloads
    fun <T : VitalMinigameInstance> registerInstance(
        instance: T,
        afterRegisterAction: (T) -> Unit = {},
    ) {
        if (vitalMinigameInstanceRepository.existsById(instance.id)) {
            throw IllegalStateException()
        }

        // Write to a marker-file so Vital can identify leftover instance worlds on restart.
        val markerFile = Bukkit.getWorldContainer().resolve(instance.world.name).resolve(VitalMinigameInstance.MARKER)
        if (!markerFile.exists()) {
            try {
                val isFileCreated = markerFile.createNewFile()
                if (!isFileCreated) {
                    throw VitalMinigameInstanceException.CreateMarker(instance.world.name, instance.id)
                }
            } catch (e: Exception) {
                throw VitalMinigameInstanceException.CreateMarker(instance.world.name, instance.id, e)
            }
        }

        vitalMinigameInstanceRepository.save(instance)

        try {
            afterRegisterAction(instance)
        } catch (e: Exception) {
            throw VitalMinigameInstanceException.ExecuteAfterRegisterAction(instance.world.name, instance.id, e)
        }

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
        unregisterInstance(instance, beforeDeleteAction, afterDeleteAction)
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

        try {
            beforeDeleteAction(instance)
        } catch (e: Exception) {
            throw VitalMinigameInstanceException.ExecuteBeforeDeleteAction(instance.world.name, instance.id, e)
        }

        instance.unregister()

        val isWorldUnloaded = Bukkit.unloadWorld(instance.world, false)
        if (!isWorldUnloaded) {
            throw VitalMinigameInstanceException.UnloadWorld(instance.world.name, instance.id)
        }

        val isDeleted = Bukkit.getWorldContainer().resolve(instance.world.name).deleteRecursively()
        if (!isDeleted) {
            throw VitalMinigameInstanceException.Delete(instance.world.name, instance.id)
        }

        try {
            afterDeleteAction(instance)
        } catch (e: Exception) {
            throw VitalMinigameInstanceException.ExecuteAfterDeleteAction(instance.world.name, instance.id, e)
        }
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
