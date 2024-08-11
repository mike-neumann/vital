package me.vitalframework;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Abstract class for managing a list of Vital components.
 * Provides methods to register, unregister, and retrieve components from the list.
 *
 * @param <T> The type of Vital components managed by this class.
 * @author xRa1ny
 */
public abstract class VitalRepository<T extends VitalComponent> {
    @Getter
    private final List<T> components = new ArrayList<>();

    /**
     * Gets a {@link List} of all {@link VitalComponent} instance matching the supplied class.
     *
     * @param clazz The class to match the registered {@link VitalComponent} of this manager with.
     * @param <X>   The type of the {@link VitalComponent} to match with.
     * @return A {@link List} of all {@link VitalComponent} instances matching the supplied class.
     */
    public final <X> List<X> getComponents(@NonNull Class<X> clazz) {
        return components.stream()
                .filter(vitalComponent -> clazz.isAssignableFrom(vitalComponent.getClass()))
                .map(clazz::cast)
                .toList();
    }

    /**
     * Checks if a VitalComponent is registered with the specified UUID.
     *
     * @param uniqueId The UUID of the VitalComponent.
     * @return true if a VitalComponent is registered with the specified UUID, false otherwise.
     */
    public final boolean isComponentRegisteredByUuid(@NonNull UUID uniqueId) {
        return Optional.ofNullable(getComponentByUniqueId(uniqueId)).isPresent();
    }

    /**
     * Checks if a VitalComponent is registered with the specified name.
     *
     * @param name The name of the VitalComponent.
     * @return true if a VitalComponent is registered with the specified name, false otherwise.
     */
    public final boolean isComponentRegisteredByName(@NonNull String name) {
        return Optional.ofNullable(getComponentByName(name)).isPresent();
    }

    /**
     * Checks if a VitalComponent is registered with the specified class.
     *
     * @param clazz The class of the VitalComponent.
     * @return true if a VitalComponent is registered with the specified class, false otherwise.
     */
    public final boolean isComponentRegisteredByClass(@NonNull Class<? extends T> clazz) {
        return Optional.ofNullable(getComponentByClass(clazz)).isPresent();
    }

    /**
     * Checks if the given {@link VitalComponent} is registered on this manager.
     *
     * @param component The {@link VitalComponent}.
     * @return true if the {@link VitalComponent} is registered, false otherwise.
     */
    public final boolean isComponentRegistered(@NonNull T component) {
        return components.contains(component);
    }

    /**
     * Gets the VitalComponent by the specified uniqueId.
     *
     * @param uniqueId The uniqueId of the VitalComponent.
     * @return THe fetched component; or null.
     */
    @Nullable
    public final T getComponentByUniqueId(@NonNull UUID uniqueId) {
        return components.stream()
                .filter(vitalComponent -> uniqueId.equals(vitalComponent.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the VitalComponent by the specified name.
     *
     * @param name The name of the VitalComponent.
     * @return The fetched component; or null.
     */
    @Nullable
    public final T getComponentByName(@NonNull String name) {
        return components.stream()
                .filter(vitalComponent -> name.equals(vitalComponent.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the VitalComponent by the specified class.
     *
     * @param clazz The class of the VitalComponent.
     * @param <X>   The {@link VitalComponent} type to grab from this manager instance.
     * @return The fetched component; or null.
     */
    @Nullable
    public final <X extends VitalComponent> X getComponentByClass(@NonNull Class<X> clazz) {
        return components.stream()
                .filter(vitalComponent -> clazz.equals(vitalComponent.getClass()))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a random VitalComponent, matching the given {@link Predicate} registered on this manager instance.
     *
     * @param predicate The predicate for filtering.
     * @return The fetched component; or null.
     */
    @Nullable
    public final T getRandomComponent(@NonNull Predicate<T> predicate) {
        if (components.isEmpty()) {
            return null;
        }

        final List<T> filteredVitalComponentList = getComponents().stream()
                .filter(predicate)
                .toList();
        final int randomIndex = new Random().nextInt(filteredVitalComponentList.size());

        return filteredVitalComponentList.get(randomIndex);
    }

    /**
     * Gets a random VitalComponent registered on this manager instance.
     *
     * @return The fetched component; or null.
     */
    @Nullable
    public final T getRandomComponent() {
        return getRandomComponent(t -> true);
    }

    /**
     * Registers the specified VitalComponent.
     *
     * @param component The VitalComponent to register.
     */
    public final void registerComponent(@NonNull T component) {
        if (isComponentRegistered(component)) {
            return;
        }

        components.add(component);
        onComponentRegistered(component);
        component.onRegistered();
    }

    /**
     * Unregisters the specified VitalComponent.
     *
     * @param component The VitalComponent to unregister.
     */
    public final void unregisterComponent(@NonNull T component) {
        if (!isComponentRegistered(component)) {
            return;
        }

        components.remove(component);
        component.onUnregistered();
        onComponentUnregistered(component);
    }

    /**
     * Attempts to unregister a {@link VitalComponent} by its specified class.
     *
     * @param clazz The class of the {@link VitalComponent} to unregister.
     */
    public final void unregisterComponentByClass(@NonNull Class<? extends T> clazz) {
        final Optional<? extends T> optionalInstance = Optional.ofNullable(getComponentByClass(clazz));

        optionalInstance.ifPresent(this::unregisterComponent);
    }

    /**
     * Called when the specified VitalComponent is registered.
     *
     * @param component The VitalComponent registered.
     */
    public void onComponentRegistered(@NonNull T component) {

    }

    /**
     * Called when the specified VitalComponent is unregistered.
     *
     * @param component The VitalComponent unregistered.
     */
    public void onComponentUnregistered(@NonNull T component) {

    }
}