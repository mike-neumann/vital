package me.xra1ny.vital.minigames;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;
import me.xra1ny.essentia.inject.DIFactory;
import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.Vital;
import me.xra1ny.vital.VitalComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

/**
 * Manages the current state of a minigame using the Vital framework.
 *
 * @author xRa1ny
 * @apiNote This class may be extended from, to add more specific mini-game manager logic or function, depending on the mini-game you are trying to implement.
 */
@Log
@Component(dependsOn = VitalMinigamesSubModule.class)
public class VitalMinigameManager implements VitalComponent {
    private static VitalMinigameManager instance;

    private final Vital<?> vital;

    private final JavaPlugin plugin;

    /**
     * The currently active minigame state.
     */
    @Getter
    private VitalMinigameState vitalMinigameState;

    /**
     * Constructs a new vital minigame manager for managing active minigame states.
     *
     * @param vital  Vital
     * @param plugin The associated java plugin.
     */
    public VitalMinigameManager(Vital<?> vital, JavaPlugin plugin) {
        this.vital = vital;
        this.plugin = plugin;
    }

    /**
     * Checks if the current minigame state matches a specified class.
     *
     * @param vitalMinigameStateClass The class of the minigame state to compare with.
     * @param <T>                     The type of minigame state.
     * @return True if the current state is of the specified class, otherwise false.
     */
    public static <T extends VitalMinigameState> boolean isVitalMinigameState(@NonNull Class<T> vitalMinigameStateClass) {
        if (instance.vitalMinigameState == null) {
            return false;
        }

        return vitalMinigameStateClass.equals(instance.vitalMinigameState.getClass());
    }

    /**
     * Sets the current minigame state by Class.
     *
     * @param vitalMinigameStateClass The Class of the minigame state to set to (must be registered).
     * @apiNote this method attempts to construct a dependency injected instance using Vital's DI utils {@link DIFactory}
     */
    public static void setVitalMinigameState(@NonNull Class<? extends VitalMinigameState> vitalMinigameStateClass) {
        final VitalMinigameState vitalMinigameState = Optional.ofNullable(instance.vital.getComponentByType(vitalMinigameStateClass))
                .orElseThrow(() -> new RuntimeException("attempted setting unregistered minigame state %s"
                        .formatted(vitalMinigameStateClass.getSimpleName())));

        setVitalMinigameState(vitalMinigameState);
    }

    /**
     * Sets the current minigame state.
     * If a previous state exists, it is unregistered before registering the new state.
     *
     * @param vitalMinigameState The new minigame state to set.
     */
    public static void setVitalMinigameState(@NonNull VitalMinigameState vitalMinigameState) {
        if (instance.vitalMinigameState != null) {
            if (instance.vitalMinigameState instanceof VitalCountdownMinigameState vitalCountdownMinigameState) {
                vitalCountdownMinigameState.stopCountdown();
            }

            // unregister listener from bukkit.
            HandlerList.unregisterAll(instance.vitalMinigameState);
            instance.vitalMinigameState.onDisable();
        }

        instance.vitalMinigameState = vitalMinigameState;
        Bukkit.getPluginManager().registerEvents(vitalMinigameState, instance.plugin);
        vitalMinigameState.onEnable();
    }

    @Override
    public void onRegistered() {
        instance = this;
    }

    @Override
    public void onUnregistered() {

    }
}