package me.vitalframework.minigames

import me.vitalframework.SpigotPlugin
import me.vitalframework.tasks.VitalCountdownTask

/**
 * Represents an abstract base class for minigame states that operate with a countdown timer.
 * This class combines countdown task management with the concept of defining different
 * states for a minigame, offering both countdown behavior and lifecycle hooks for the state.
 *
 * The class extends `VitalCountdownTask.Spigot` to provide countdown functionalities specifically
 * tailored for a Spigot-based environment and implements the `VitalBaseMinigameState` interface,
 * enabling seamless integration with a minigame's state management system.
 *
 * Subclasses are expected to define specific behaviors for their minigame state, using
 * the countdown management features provided by the superclass and handling their custom
 * logic during the enabled/disabled state transitions.
 *
 * @constructor Creates a new instance of `VitalCountdownMinigameState`.
 * @param plugin The Spigot plugin instance used to manage tasks and register listeners.
 */
abstract class VitalCountdownMinigameState(
    plugin: SpigotPlugin,
) : VitalCountdownTask.Spigot(plugin),
    VitalBaseMinigameState
