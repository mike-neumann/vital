package me.vitalframework.minigames;

import me.vitalframework.VitalComponent;
import org.bukkit.event.Listener;

/**
 * Abstract base class for minigame states within the Vital framework.
 * Minigame states define specific phases or conditions in a minigame.
 * Extend this class to create custom minigame states.
 *
 * @author xRa1ny
 */
public interface VitalMinigameState extends VitalComponent, Listener {
    @Override
    default void onRegistered() {

    }

    @Override
    default void onUnregistered() {

    }

    /**
     * Called when this state is enabled.
     */
    default void onEnable() {

    }

    /**
     * Called when this state is disabled.
     */
    default void onDisable() {

    }
}