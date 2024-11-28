package me.vitalframework.minigames;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.vitalframework.Vital;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.stereotype.Service;

/**
 * Manages the current state of a minigame using the Vital framework.
 *
 * @author xRa1ny
 */
@Getter
@RequiredArgsConstructor
@Service
public class VitalMinigameService {
    @NonNull
    private final JavaPlugin plugin;

    private VitalBaseMinigameState vitalMinigameState;

    /**
     * Checks if the current minigame state matches a specified class.
     *
     * @param vitalMinigameStateClass The class of the minigame state to compare with.
     * @param <T>                     The type of minigame state.
     * @return True if the current state is of the specified class, otherwise false.
     */
    public <T extends VitalBaseMinigameState> boolean isVitalMinigameState(@NonNull Class<T> vitalMinigameStateClass) {
        if (vitalMinigameState == null) {
            return false;
        }

        return vitalMinigameStateClass.equals(vitalMinigameState.getClass());
    }

    /**
     * Sets the current minigame state by Class.
     *
     * @param vitalMinigameStateClass The Class of the minigame state to set to (must be registered).
     * @apiNote this method attempts to construct a dependency injected instance
     */
    public void setVitalMinigameState(@NonNull Class<? extends VitalBaseMinigameState> vitalMinigameStateClass) {
        final var minigameState = Vital.getContext().getBean(vitalMinigameStateClass);

        setVitalMinigameState(minigameState);
    }

    /**
     * Sets the current minigame state.
     * If a previous state exists, it is unregistered before registering the new state.
     *
     * @param minigameState The new minigame state to set.
     */
    public void setVitalMinigameState(@NonNull VitalBaseMinigameState minigameState) {
        if (this.vitalMinigameState != null) {
            if (this.vitalMinigameState instanceof VitalCountdownMinigameState vitalCountdownMinigameState) {
                vitalCountdownMinigameState.stop();
            }

            // unregister listener from bukkit.
            HandlerList.unregisterAll(this.vitalMinigameState);
            this.vitalMinigameState.onDisable();
        }

        this.vitalMinigameState = minigameState;
        Bukkit.getPluginManager().registerEvents(minigameState, plugin);
        minigameState.onEnable();
    }
}