package me.vitalframework.minigames

/**
 * Represents the default implementation of a minigame state.
 * This class extends the `VitalBaseMinigameState` interface and can be used
 * as a simple or base implementation for minigames that do not need complex state logic.
 *
 * This state is expected to be used within a minigame state management system,
 * allowing it to participate in state transitions and lifecycle events such as
 * `onEnable` and `onDisable`.
 *
 * The main purpose of this class is to provide a concrete implementation
 * of the `VitalBaseMinigameState` interface with minimal custom behavior,
 * serving as a foundation for more specialized states.
 */
class VitalMinigameState : VitalBaseMinigameState