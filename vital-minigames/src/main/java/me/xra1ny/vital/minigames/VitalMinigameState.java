package me.xra1ny.vital.minigames;

import me.xra1ny.vital.core.VitalComponent;
import org.bukkit.event.Listener;

/**
 * Abstract base class for minigame states within the Vital framework.
 * Minigame states define specific phases or conditions in a minigame.
 * Extend this class to create custom minigame states.
 *
 * @author xRa1ny
 */
public abstract class VitalMinigameState implements VitalComponent, Listener {
    @Override
    public void onRegistered() {

    }

    @Override
    public void onUnregistered() {

    }
}