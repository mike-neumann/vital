package me.vitalframework.minigames;

import lombok.Getter;
import me.vitalframework.tasks.VitalCountdownTask;

import java.util.UUID;

/**
 * An abstract class for countdown-based {@link VitalBaseMinigameState} in the Vital framework.
 *
 * @author xRa1ny
 */
public abstract class VitalCountdownMinigameState extends VitalCountdownTask.Spigot implements VitalBaseMinigameState {
    @Getter
    private final UUID uniqueId = UUID.randomUUID();
}