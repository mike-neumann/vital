package me.vitalframework.minigames;

import lombok.Getter;
import lombok.NonNull;
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
@Service
public class VitalMinigameService {
    private final JavaPlugin plugin;

    @Getter
    private VitalMinigameState vitalMinigameState;

    public VitalMinigameService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the current minigame state matches a specified class.
     *
     * @param vitalMinigameStateClass The class of the minigame state to compare with.
     * @param <T>                     The type of minigame state.
     * @return True if the current state is of the specified class, otherwise false.
     */
    public <T extends VitalMinigameState> boolean isVitalMinigameState(@NonNull Class<T> vitalMinigameStateClass) {
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
    public void setVitalMinigameState(@NonNull Class<? extends VitalMinigameState> vitalMinigameStateClass) {
        final VitalMinigameState vitalMinigameState = Vital.getContext().getBean(vitalMinigameStateClass);

        setVitalMinigameState(vitalMinigameState);
    }

    /**
     * Sets the current minigame state.
     * If a previous state exists, it is unregistered before registering the new state.
     *
     * @param vitalMinigameState The new minigame state to set.
     */
    public void setVitalMinigameState(@NonNull VitalMinigameState vitalMinigameState) {
        if (this.vitalMinigameState != null) {
            if (this.vitalMinigameState instanceof VitalCountdownMinigameState vitalCountdownMinigameState) {
                vitalCountdownMinigameState.stop();
            }

            // unregister listener from bukkit.
            HandlerList.unregisterAll(this.vitalMinigameState);
            this.vitalMinigameState.onDisable();
        }

        this.vitalMinigameState = vitalMinigameState;
        Bukkit.getPluginManager().registerEvents(vitalMinigameState, plugin);
        vitalMinigameState.onEnable();
    }
}