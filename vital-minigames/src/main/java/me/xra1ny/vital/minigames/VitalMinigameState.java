package me.xra1ny.vital.minigames;

import me.xra1ny.vital.VitalComponent;
import org.bukkit.event.Listener;
import org.springframework.stereotype.Component;

/**
 * Abstract base class for minigame states within the Vital framework.
 * Minigame states define specific phases or conditions in a minigame.
 * Extend this class to create custom minigame states.
 *
 * @author xRa1ny
 */
@Component
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