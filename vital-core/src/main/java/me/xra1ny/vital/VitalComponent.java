package me.xra1ny.vital;

import lombok.NonNull;

import java.util.UUID;

/**
 * Interface representing a component within the Vital-Framework.
 * Defines common methods and lifecycle events for Vital components.
 *
 * @author xRa1ny
 * @apiNote {@link VitalComponent} may be everything within a plugin, a command, a config, a game state, a player, an item, etc.
 */
public interface VitalComponent {
    /**
     * Gets the unique identifier for this component.
     *
     * @return The {@link UUID} representing this component.
     */
    @NonNull
    default UUID getUniqueId() {
        return UUID.randomUUID();
    }

    /**
     * Gets the name of this component.
     *
     * @return The name of the component (usually the class name).
     */
    @NonNull
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Called when this {@link VitalComponent} is registered.
     * Implement this method to perform any required initialization when the component is registered.
     */
    void onRegistered();

    /**
     * Called when this {@link VitalComponent} is unregistered.
     * Implement this method to perform any cleanup or finalization when the component is unregistered.
     */
    void onUnregistered();
}