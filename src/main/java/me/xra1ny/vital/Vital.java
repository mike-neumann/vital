package me.xra1ny.vital;

import lombok.SneakyThrows;
import me.xra1ny.vital.commands.VitalCommandManager;
import me.xra1ny.vital.configs.VitalConfigManager;
import me.xra1ny.vital.core.VitalCore;
import me.xra1ny.vital.core.VitalListenerManager;
import me.xra1ny.vital.databases.VitalDatabaseManager;
import me.xra1ny.vital.holograms.VitalHologramConfig;
import me.xra1ny.vital.holograms.VitalHologramManager;
import me.xra1ny.vital.inventories.VitalInventoryListener;
import me.xra1ny.vital.items.VitalItemStackCooldownHandler;
import me.xra1ny.vital.items.VitalItemStackListener;
import me.xra1ny.vital.items.VitalItemStackManager;
import me.xra1ny.vital.players.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The main class of the Vital-Framework.
 *
 * @param <T> Your plugins main class.
 * @author xRa1ny
 * @apiNote This class is used for a complete package of Vital.
 */
@SuppressWarnings("unused")
public final class Vital<T extends JavaPlugin> extends VitalCore<T> {
    private static Vital<?> instance;

    public Vital(@NotNull T javaPlugin) {
        super(javaPlugin);
    }

    @Override
    public void onEnable() {
        // Register VitalConfigManagement
        final VitalConfigManager vitalConfigManager = new VitalConfigManager();
        final DefaultVitalConfig defaultVitalConfig = new DefaultVitalConfig(getJavaPlugin());

        getVitalComponentManager().registerVitalComponent(vitalConfigManager);
        getVitalComponentManager().registerVitalComponent(defaultVitalConfig);

        // Register VitalPlayerManagement if no other player management has yet been registered by implementing programmer.
        final VitalListenerManager vitalListenerManager = getVitalListenerManager().get();
        final DefaultVitalPlayerManager defaultVitalPlayerManager = new DefaultVitalPlayerManager(defaultVitalConfig.vitalPlayerTimeout);
        final DefaultVitalPlayerListener defaultVitalPlayerListener = new DefaultVitalPlayerListener(getJavaPlugin(), defaultVitalPlayerManager);
        final DefaultVitalPlayerTimeoutHandler defaultVitalPlayerTimeoutHandler = new DefaultVitalPlayerTimeoutHandler(getJavaPlugin(), defaultVitalPlayerManager);

        getVitalComponentManager().registerVitalComponent(defaultVitalPlayerManager);
        vitalListenerManager.registerVitalComponent(defaultVitalPlayerListener);
        getVitalComponentManager().registerVitalComponent(defaultVitalPlayerTimeoutHandler);

        // Register VitalCommandManagement
        final VitalCommandManager vitalCommandManager = new VitalCommandManager(getJavaPlugin());

        getVitalComponentManager().registerVitalComponent(vitalCommandManager);

        // Register VitalHologramManagement
        final VitalHologramConfig vitalHologramConfig = new VitalHologramConfig(getJavaPlugin());
        final VitalHologramManager vitalHologramManager = new VitalHologramManager(getJavaPlugin(), vitalHologramConfig);

        getVitalComponentManager().registerVitalComponent(vitalHologramManager);

        // Register VitalItemStackManagement and VitalItemStackCooldownHandler
        final VitalItemStackManager vitalItemStackManager = new VitalItemStackManager(getJavaPlugin());
        final VitalItemStackListener vitalItemStackListener = new VitalItemStackListener(vitalItemStackManager);
        final VitalItemStackCooldownHandler vitalItemStackCooldownHandler = new VitalItemStackCooldownHandler(getJavaPlugin(), vitalItemStackManager);

        getVitalComponentManager().registerVitalComponent(vitalItemStackManager);
        vitalListenerManager.registerVitalComponent(vitalItemStackListener);
        getVitalComponentManager().registerVitalComponent(vitalItemStackCooldownHandler);

        // Register VitalInventoryMenuListener
        final VitalInventoryListener vitalInventoryListener = new VitalInventoryListener();

        vitalListenerManager.registerVitalComponent(vitalInventoryListener);

        if (defaultVitalConfig.vitalDatabaseEnabled) {
            // Register VitalDatabaseManager
            final VitalDatabaseManager vitalDatabaseManager = new VitalDatabaseManager();

            getVitalComponentManager().registerVitalComponent(vitalDatabaseManager);
        }

        // finally register this instance for singleton access point.
        instance = this;
    }

    /**
     * Unregisters Vital's default player management system.
     *
     * @apiNote This method is implemented for convenience for registering custom player management.
     */
    public void unregisterDefaultVitalPlayerManagement() {
        final Optional<DefaultVitalPlayerManager> optionalDefaultVitalPlayerManager = getVitalComponentManager().getVitalComponent(DefaultVitalPlayerManager.class);
        final Optional<DefaultVitalPlayerListener> optionalDefaultVitalPlayerListener = getVitalComponentManager().getVitalComponent(DefaultVitalPlayerListener.class);
        final Optional<DefaultVitalPlayerTimeoutHandler> optionalDefaultVitalPlayerTimeoutHandler = getVitalComponentManager().getVitalComponent(DefaultVitalPlayerTimeoutHandler.class);

        optionalDefaultVitalPlayerManager.ifPresent(getVitalComponentManager()::unregisterVitalComponent);
        optionalDefaultVitalPlayerListener.ifPresent(getVitalComponentManager()::unregisterVitalComponent);
        optionalDefaultVitalPlayerTimeoutHandler.ifPresent(getVitalComponentManager()::unregisterVitalComponent);
    }

    /**
     * Registers custom player management, unregistering default player management if not already.
     *
     * @param customVitalPlayerClass               The custom {@link VitalPlayer} class.
     * @param customVitalPlayerManagerClass        The custom {@link VitalPlayerManager} class.
     * @param customVitalPlayerListenerClass       The custom {@link VitalPlayerListener} class.
     * @param customVitalPlayerTimeoutHandlerClass The custom {@link VitalPlayerTimeoutHandler} class.
     * @param customVitalPlayerTimeout             The custom player timeout.
     * @param <P>                                  {@link VitalPlayer}
     * @param <M>                                  {@link VitalPlayerManager}
     * @param <L>                                  {@link VitalPlayerListener}
     * @param <TH>                                 {@link VitalPlayerTimeoutHandler}
     * @apiNote The specified VitalPlayerManagement Classes MUST contain the DefaultVitalPlayerManagement Constructors.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @SneakyThrows
    public <P extends VitalPlayer, M extends VitalPlayerManager<P>, L extends VitalPlayerListener<P>, TH extends VitalPlayerTimeoutHandler<P>> void registerSimpleCustomPlayerManagement(@NotNull Class<P> customVitalPlayerClass, @NotNull Class<M> customVitalPlayerManagerClass, @NotNull Class<L> customVitalPlayerListenerClass, @NotNull Class<TH> customVitalPlayerTimeoutHandlerClass, int customVitalPlayerTimeout) {
        unregisterDefaultVitalPlayerManagement();

        final VitalListenerManager vitalListenerManager = getVitalComponentManager().getVitalComponent(VitalListenerManager.class).get();
        final M customVitalPlayerManager = customVitalPlayerManagerClass.getDeclaredConstructor().newInstance();
        final L customVitalPlayerListener = customVitalPlayerListenerClass.getDeclaredConstructor(JavaPlugin.class, VitalPlayerManager.class, int.class).newInstance(getJavaPlugin(), customVitalPlayerManager, customVitalPlayerTimeout);
        final TH customVitalPlayerTimeoutHandler = customVitalPlayerTimeoutHandlerClass.getDeclaredConstructor(JavaPlugin.class, VitalPlayerManager.class).newInstance(getJavaPlugin(), customVitalPlayerManager);

        getVitalComponentManager().registerVitalComponent(customVitalPlayerManager);
        vitalListenerManager.registerVitalComponent(customVitalPlayerListener);
        getVitalComponentManager().registerVitalComponent(customVitalPlayerTimeoutHandler);
    }

    /**
     * Registers custom player management using the specified {@link VitalPlayer} class, instantiating every needed dependency required for player management.
     *
     * @param customVitalPlayerClass   The custom {@link VitalPlayer} class.
     * @param customVitalPlayerTimeout The custom player timeout.
     * @param <P>                      {@link VitalPlayer}
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public <P extends VitalPlayer> void registerSimpleCustomPlayerManagement(@NotNull Class<P> customVitalPlayerClass, int customVitalPlayerTimeout) {
        unregisterDefaultVitalPlayerManagement();

        final VitalListenerManager vitalListenerManager = getVitalComponentManager().getVitalComponent(VitalListenerManager.class).get();
        final CustomVitalPlayerManager<P> customVitalPlayerManager = new CustomVitalPlayerManager<>(customVitalPlayerTimeout);
        final CustomVitalPlayerListener<P> customVitalPlayerListener = new CustomVitalPlayerListener<>(getJavaPlugin(), customVitalPlayerManager, customVitalPlayerClass);
        final CustomVitalPlayerTimeoutHandler<P> customVitalPlayerTimeoutHandler = new CustomVitalPlayerTimeoutHandler<>(getJavaPlugin(), customVitalPlayerManager);

        getVitalComponentManager().registerVitalComponent(customVitalPlayerManager);
        vitalListenerManager.registerVitalComponent(customVitalPlayerListener);
        getVitalComponentManager().registerVitalComponent(customVitalPlayerTimeoutHandler);
    }

    public Optional<VitalConfigManager> getVitalConfigManager() {
        return getVitalComponentManager().getVitalComponent(VitalConfigManager.class);
    }

    public Optional<VitalListenerManager> getVitalListenerManager() {
        return getVitalComponentManager().getVitalComponent(VitalListenerManager.class);
    }

    public Optional<DefaultVitalPlayerManager> getDefaultVitalPlayerManager() {
        return getVitalComponentManager().getVitalComponent(DefaultVitalPlayerManager.class);
    }

    public Optional<DefaultVitalPlayerListener> getDefaultVitalPlayerListener() {
        return getVitalComponentManager().getVitalComponent(DefaultVitalPlayerListener.class);
    }

    public Optional<DefaultVitalPlayerTimeoutHandler> getDefaultVitalPlayerTimeoutHandler() {
        return getVitalComponentManager().getVitalComponent(DefaultVitalPlayerTimeoutHandler.class);
    }

    @SuppressWarnings("rawtypes")
    public Optional<CustomVitalPlayerManager> getCustomVitalPlayerManager() {
        return getVitalComponentManager().getVitalComponent(CustomVitalPlayerManager.class);
    }

    @SuppressWarnings("rawtypes")
    public Optional<CustomVitalPlayerListener> getCustomVitalPlayerListener() {
        return getVitalComponentManager().getVitalComponent(CustomVitalPlayerListener.class);
    }

    @SuppressWarnings("rawtypes")
    public Optional<CustomVitalPlayerTimeoutHandler> getCustomVitalPlayerTimeoutHandler() {
        return getVitalComponentManager().getVitalComponent(CustomVitalPlayerTimeoutHandler.class);
    }

    public Optional<VitalCommandManager> getVitalCommandManager() {
        return getVitalComponentManager().getVitalComponent(VitalCommandManager.class);
    }

    public Optional<VitalHologramManager> getVitalHologramManager() {
        return getVitalComponentManager().getVitalComponent(VitalHologramManager.class);
    }

    public Optional<VitalItemStackManager> getVitalItemStackManager() {
        return getVitalComponentManager().getVitalComponent(VitalItemStackManager.class);
    }

    public Optional<VitalDatabaseManager> getVitalDatabaseManager() {
        return getVitalComponentManager().getVitalComponent(VitalDatabaseManager.class);
    }

    /**
     * Singleton access-point for {@link Vital} instance.
     *
     * @param type Your plugin's main Class.
     * @param <T>  The type of your plugin's main class.
     * @return The {@link Vital} instance.
     * @throws ClassCastException If the provided type and {@link Vital} plugin instance don't match.
     */
    @SuppressWarnings("unchecked")
    public static <T extends JavaPlugin> Vital<T> getVitalInstance(@NotNull Class<T> type) {
        return (Vital<T>) instance;
    }
}