package me.xra1ny.vital;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import me.xra1ny.essentia.inject.DIContainer;
import me.xra1ny.essentia.inject.EssentiaInject;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.util.*;

/**
 * The main instance of the Vital-Framework.
 *
 * @param <Plugin> The Plugin type.
 * @author xRa1ny
 */
@SuppressWarnings("unused")
@Log
public class Vital<Plugin> implements DIContainer {
    private static Vital<?> instance;

    /**
     * Holds a list of all classes registered on this classpath for later use of dependency injection.
     *
     * @apiNote This implementation is used to improve performance across many managers since scanning takes some time.
     */
    @Getter
    @NonNull
    private static final Set<Class<? extends VitalComponent>> scannedClassSet = new Reflections().getSubTypesOf(VitalComponent.class);

    @Getter
    @NonNull
    private final Map<Class<?>, Object> componentClassObjectMap = new HashMap<>();

    /**
     * The plugin instance associated with this {@link Vital}.
     */
    @Getter
    @NonNull
    private final Plugin plugin;

    /**
     * Constructs a new {@link Vital} instance.
     *
     * @param plugin The {@link JavaPlugin} instance to associate with {@link Vital}.
     */
    public Vital(@NonNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Singleton access-point for all {@link Vital} instances.
     *
     * @param type Your plugin's main class.
     * @param <T>  The type of your plugin's main class.
     * @return The {@link Vital} instance.
     * @throws ClassCastException If the provided type and {@link Vital} plugin instance don't match.
     */
    @SuppressWarnings("unchecked")
    public static <T extends JavaPlugin> Vital<T> getVitalCoreInstance(@NonNull Class<T> type) {
        return (Vital<T>) instance;
    }

    /**
     * Singleton access-point for {@link Vital} instance.
     * This method will return a generically inaccurate Object.
     * For more accurate {@link Vital} types use {@link Vital#getVitalCoreInstance(Class)}
     *
     * @return The {@link Vital} instance.
     */
    public static Vital<?> getVitalCoreInstance() {
        return instance;
    }

    @Override
    public void unregisterComponentByType(@NonNull Class<?> type) {
        componentClassObjectMap.remove(type);
    }

    @Override
    public void unregisterComponent(@NonNull Object o) {
        componentClassObjectMap.remove(o.getClass(), o);

        if (o instanceof VitalComponent vitalComponent) {
            vitalComponent.onUnregistered();
        }
    }

    @Override
    public void registerComponent(@NonNull Object o) {
        if (isRegistered(o)) {
            return;
        }

        componentClassObjectMap.put(o.getClass(), o);

        if (o instanceof VitalComponent vitalComponent) {
            vitalComponent.onRegistered();
        }
    }

    /**
     * Enables the Vital-Framework, initialising needed systems.
     */
    @SneakyThrows // TODO
    public final void enable() {
        log.info("Enabling VitalCore<%s>"
                .formatted(plugin));

        instance = this;
        registerComponent(instance);
        registerComponent(plugin);

        log.info("all registered: " + componentClassObjectMap);

        // register both plugin package and Vital's package for dependency injection using essentia-inject.
        EssentiaInject.run(this, plugin.getClass().getPackageName(), getClass().getPackageName());

        log.info("VitalCore<%s> enabled!"
                .formatted(plugin));
        log.info("Hello from Vital!");
    }

    @Nullable
    public VitalComponent getComponent(@NotNull UUID uniqueId) {
        return getComponents().stream()
                .filter(VitalComponent.class::isInstance)
                .map(VitalComponent.class::cast)
                .filter(component -> component.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks whether the specified submodule is used or not.
     *
     * @param subModuleName The submodule's name (e.g. vital, vital-core, vital-commands, etc.)
     * @return True if the given submodule is used; false otherwise.
     */
    public boolean isUsingSubModule(@NonNull String subModuleName) {
        return getUsedSubModuleNames().stream()
                .map(String::toLowerCase)
                .toList()
                .contains(subModuleName.toLowerCase());
    }

    /**
     * Checks whether the specified submodule is used or not.
     *
     * @param subModuleType The submodule class.
     * @return True if the submodule is used; false otherwise.
     */
    public boolean isUsingSubModule(@NonNull Class<? extends VitalSubModule> subModuleType) {
        return isRegistered(subModuleType);
    }

    /**
     * Gets a list of all used submodules.
     *
     * @return A list of all used submodules.
     */
    @NonNull
    public List<? extends VitalSubModule> getUsedSubModules() {
        return getComponentsByType(VitalSubModule.class);
    }

    /**
     * Gets a list of all submodules by name.
     *
     * @return A list of all used submodule names.
     */
    @NonNull
    public List<String> getUsedSubModuleNames() {
        return getUsedSubModules().stream()
                .map(VitalSubModule::getName)
                .toList();
    }
}