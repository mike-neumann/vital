package me.xra1ny.vital.minigames;

import me.xra1ny.vital.tasks.VitalCountdownTask;
import org.springframework.stereotype.Component;

/**
 * An abstract class for countdown-based {@link VitalMinigameState} in the Vital framework.
 *
 * @author xRa1ny
 */
@Component
public abstract class VitalCountdownMinigameState extends VitalCountdownTask.Spigot implements VitalMinigameState {
}