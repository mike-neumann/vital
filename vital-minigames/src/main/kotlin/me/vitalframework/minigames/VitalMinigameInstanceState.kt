package me.vitalframework.minigames

import me.vitalframework.SpigotListener

/**
 * Defines a single state within an active minigame instance.
 * All event handlers registered here will only be active, when this state is active in its registered game instance.
 * A state may perform mutation on its own instance that's passed to its constructor to modify game data.
 *
 * ```java
 * public class MyMinigameInstanceState implements VitalMinigameInstanceState<MyMinigameInstance> {
 *   private MyMinigameInstance instance;
 *
 *   public MyMinigameInstance getInstance() {
 *     return instance;
 *   }
 *
 *   @Override
 *   public void onEnable() {
 *     // ...
 *   }
 *
 *   @Override
 *   public void onDisable() {
 *     // ...
 *   }
 * }
 * ```
 */
interface VitalMinigameInstanceState<T : VitalMinigameInstance> : SpigotListener {
    /**
     * The instance this state belongs to.
     * Each instance may have its own data. e.g. its designated state instances you may switch to.
     */
    val instance: T

    /**
     * Called when this state is enabled on its registered instance.
     */
    fun onEnable() {}

    /**
     * Called when this state is disabled on its registered instance.
     */
    fun onDisable() {}
}
