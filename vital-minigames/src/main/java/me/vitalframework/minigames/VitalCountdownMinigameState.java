package me.vitalframework.minigames;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.tasks.VitalCountdownTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * An abstract class for countdown-based {@link VitalBaseMinigameState} in the Vital framework.
 *
 * @author xRa1ny
 */
@Getter
public abstract class VitalCountdownMinigameState extends VitalCountdownTask.Spigot implements VitalBaseMinigameState {
    @NonNull
    private final UUID uniqueId = UUID.randomUUID();

    public VitalCountdownMinigameState(@NonNull JavaPlugin plugin) {
        super(plugin);
    }
}