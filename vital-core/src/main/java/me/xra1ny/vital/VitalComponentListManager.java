package me.xra1ny.vital;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.xra1ny.essentia.inject.DIFactory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Abstract class for managing a list of Vital components.
 * Provides methods to register, unregister, and retrieve components from the list.
 *
 * @param <T> The type of Vital components managed by this class.
 * @author xRa1ny
 */
public abstract class VitalComponentListManager<T extends VitalComponent> implements VitalComponent {
    @Getter
    @NonNull
    private final List<T> vitalComponents = new ArrayList<>();

    /**
     * Gets a {@link List} of all {@link VitalComponent} instance matching the supplied class.
     *
     * @param clazz The class to match the registered {@link VitalComponent} of this manager with.
     * @param <X>   The type of the {@link VitalComponent} to match with.
     * @return A {@link List} of all {@link VitalComponent} instances matching the supplied class.
     */
    public final <X> List<X> getVitalComponents(@NonNull Class<X> clazz) {
        return vitalComponents.stream()
                .filter(vitalComponent -> clazz.isAssignableFrom(vitalComponent.getClass()))
                .map(clazz::cast)
                .toList();
    }

    /**
     * Checks if a VitalComponent is registered with the specified UUID.
     *
     * @param vitalComponentUniqueId The UUID of the VitalComponent.
     * @return true if a VitalComponent is registered with the specified UUID, false otherwise.
     */
    public final boolean isVitalComponentRegisteredByUuid(@NonNull UUID vitalComponentUniqueId) {
        return Optional.ofNullable(getVitalComponentByUniqueId(vitalComponentUniqueId)).isPresent();
    }

    /**
     * Checks if a VitalComponent is registered with the specified name.
     *
     * @param vitalComponentName The name of the VitalComponent.
     * @return true if a VitalComponent is registered with the specified name, false otherwise.
     */
    public final boolean isVitalComponentRegisteredByName(@NonNull String vitalComponentName) {
        return Optional.ofNullable(getVitalComponentByName(vitalComponentName)).isPresent();
    }

    /**
     * Checks if a VitalComponent is registered with the specified class.
     *
     * @param vitalComponentClass The class of the VitalComponent.
     * @return true if a VitalComponent is registered with the specified class, false otherwise.
     */
    public final boolean isVitalComponentRegisteredByClass(@NonNull Class<? extends VitalComponent> vitalComponentClass) {
        return Optional.ofNullable(getVitalComponentByClass(vitalComponentClass)).isPresent();
    }

    /**
     * Checks if the given {@link VitalComponent} is registered on this manager.
     *
     * @param vitalComponent The {@link VitalComponent}.
     * @return true if the {@link VitalComponent} is registered, false otherwise.
     */
    public final boolean isVitalComponentRegistered(@NonNull T vitalComponent) {
        return vitalComponents.contains(vitalComponent);
    }

    /**
     * Gets the VitalComponent by the specified uniqueId.
     *
     * @param uniqueId The uniqueId of the VitalComponent.
     * @return THe fetched component; or null.
     */
    @Nullable
    public final T getVitalComponentByUniqueId(@NonNull UUID uniqueId) {
        return vitalComponents.stream()
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
    public final T getVitalComponentByName(@NonNull String name) {
        return vitalComponents.stream()
                .filter(vitalComponent -> name.equals(vitalComponent.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the VitalComponent by the specified class.
     *
     * @param vitalComponentClass The class of the VitalComponent.
     * @param <X>                 The {@link VitalComponent} type to grab from this manager instance.
     * @return The fetched component; or null.
     */
    @Nullable
    public final <X extends VitalComponent> X getVitalComponentByClass(@NonNull Class<X> vitalComponentClass) {
        return vitalComponents.stream()
                .filter(vitalComponent -> vitalComponentClass.equals(vitalComponent.getClass()))
                .map(vitalComponentClass::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a random VitalComponent, matching the given {@link Predicate} registered on this manager instance.
     *
     * @return The fetched component; or null.
     */
    @Nullable
    public final T getRandomVitalComponent(@NonNull Predicate<T> predicate) {
        if (vitalComponents.isEmpty()) {
            return null;
        }

        final List<T> filteredVitalComponentList = getVitalComponents().stream()
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
    public final T getRandomVitalComponent() {
        return getRandomVitalComponent(t -> true);
    }

    /**
     * Registers the specified VitalComponent.
     *
     * @param vitalComponent The VitalComponent to register.
     */
    public final void registerVitalComponent(@NonNull T vitalComponent) {
        if (isVitalComponentRegistered(vitalComponent)) {
            return;
        }

        vitalComponents.add(vitalComponent);
        onVitalComponentRegistered(vitalComponent);
        vitalComponent.onRegistered();
    }

    /**
     * Attempts to register the supplied {@link VitalComponent} class using {@link DIFactory} to create a dependency injected instance.
     *
     * @param vitalComponentClass The class of the {@link VitalComponent} to register if a dependency injected instance could be created successfully.
     */
    @SneakyThrows // TODO
    public final void registerVitalComponent(@NonNull Class<? extends T> vitalComponentClass) {
        // attempt to get dependency injected instance of parameter class.
        final Optional<? extends T> optionalInstance = Optional.ofNullable(DIFactory.getInstance(vitalComponentClass));

        optionalInstance.ifPresent(this::registerVitalComponent);
    }

    /**
     * Unregisters the specified VitalComponent.
     *
     * @param vitalComponent The VitalComponent to unregister.
     */
    public final void unregisterVitalComponent(@NonNull T vitalComponent) {
        if (!isVitalComponentRegistered(vitalComponent)) {
            return;
        }

        vitalComponents.remove(vitalComponent);
        vitalComponent.onUnregistered();
        onVitalComponentUnregistered(vitalComponent);
    }

    @Override
    public void onUnregistered() {

    }

    @Override
    public void onRegistered() {

    }

    /**
     * Attempts to unregister a {@link VitalComponent} by its specified class.
     *
     * @param vitalComponentClass The class of the {@link VitalComponent} to unregister.
     */
    public final void unregisterVitalComponentByClass(@NonNull Class<? extends T> vitalComponentClass) {
        final Optional<? extends T> optionalInstance = Optional.ofNullable(getVitalComponentByClass(vitalComponentClass));

        optionalInstance.ifPresent(this::unregisterVitalComponent);
    }

    /**
     * Called when the specified VitalComponent is registered.
     *
     * @param vitalComponent The VitalComponent registered.
     */
    public void onVitalComponentRegistered(@NonNull T vitalComponent) {

    }

    /**
     * Called when the specified VitalComponent is unregistered.
     *
     * @param vitalComponent The VitalComponent unregistered.
     */
    public void onVitalComponentUnregistered(@NonNull T vitalComponent) {

    }
}