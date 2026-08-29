package dev.vitalframework.minigames

import java.util.UUID

/**
 * Internal exception; thrown during [VitalMinigameInstance] lifecycles.
 */
abstract class VitalMinigameInstanceException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Internal exception; thrown when an exception occurs during instance registration.
     */
    class Register(
        instanceId: UUID,
        instanceClass: Class<out VitalMinigameInstance>,
        cause: Throwable,
    ) : VitalMinigameInstanceException(
            "Error while registering instance with id '$instanceId' and class '${instanceClass.simpleName}'.",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs during instance unregistration.
     */
    class Unregister(
        instanceId: UUID,
        instanceClass: Class<out VitalMinigameInstance>,
        cause: Throwable,
    ) : VitalMinigameInstanceException(
            "Error while unregistering instance with id '$instanceId' and class '${instanceClass.simpleName}'.",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs during event handler registration.
     */
    class RegisterEventHandlers(
        eventHandlerClass: Class<*>,
        instanceId: UUID,
        instanceClass: Class<out VitalMinigameInstance>,
        cause: Throwable,
    ) : VitalMinigameInstanceException(
            "Error while registering event handlers in class '${eventHandlerClass.simpleName}' for instance with id '$instanceId' and class '${instanceClass.simpleName}'.",
            cause,
        )

    /**
     * Internal exception; thrown when the world of an instance could not be unloaded.
     */
    class UnloadWorld(
        worldName: String,
        instanceId: UUID,
    ) : VitalMinigameInstanceException("Could not unload world '$worldName' for instance with id '$instanceId'.")

    /**
     * Internal exception; thrown when the world of an instance could not be deleted.
     */
    class Delete(
        worldName: String,
        instanceId: UUID,
    ) : VitalMinigameInstanceException("Could not delete world '$worldName' for instance with id '$instanceId'.")

    /**
     * Internal exception; thrown when an exception occurs while executing the after deletion action during instance unregistration.
     */
    class ExecuteAfterDeleteAction(
        worldName: String,
        instanceId: UUID,
        cause: Throwable,
    ) : VitalMinigameInstanceException(
            "Error while executing after delete action during instance unregistration for instance with world '$worldName' and id '$instanceId'.",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs while executing the before deletion action during instance unregistration.
     */
    class ExecuteBeforeDeleteAction(
        worldName: String,
        instanceId: UUID,
        cause: Throwable,
    ) : VitalMinigameInstanceException(
            "Error while executing before delete action during instance unregistration for instance with world '$worldName' and id '$instanceId'.",
            cause,
        )

    /**
     * Internal exception; thrown when the internal marker file for an instance world could not be created.
     */
    class CreateMarker(
        worldName: String,
        instanceId: UUID,
        cause: Throwable? = null,
    ) : VitalMinigameInstanceException(
            "Error while creating internal marker file for instance with world '$worldName' and id '$instanceId'.'",
            cause,
        )

    /**
     * Internal exception; thrown when an exception occurs while executing the after register action.
     */
    class ExecuteAfterRegisterAction(
        worldName: String,
        instanceId: UUID,
        cause: Throwable,
    ) : VitalMinigameInstanceException(
            "Error while executing after register action for instance with world '$worldName' and id '$instanceId'.",
            cause,
        )

    /**
     * Internal exception; thrown when a template world is loaded while the plugin is disabled.
     */
    class LoadTemplateWorldPluginDisabled(
        templateWorldName: String,
        pluginName: String,
    ) : VitalMinigameInstanceException("Cannot load template world '$templateWorldName', plugin '$pluginName' is disabled.")

    /**
     * Internal exception; thrown when an exception occurs while loading a template world.
     */
    class ExecuteLoadTemplateWorldAction(
        templateWorldName: String,
        pluginName: String,
        cause: Throwable,
    ) : VitalMinigameInstanceException("Error while loading template world '$templateWorldName' for plugin '$pluginName'.", cause)
}
