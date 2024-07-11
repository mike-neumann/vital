package me.xra1ny.vital.minigames;

import me.xra1ny.vital.RequiresAnnotation;
import me.xra1ny.vital.tasks.VitalCountdownTask;
import me.xra1ny.vital.tasks.VitalRepeatableTask;
import me.xra1ny.vital.tasks.annotation.VitalCountdownTaskInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An abstract class for countdown-based {@link VitalMinigameState} in the Vital framework.
 *
 * @author xRa1ny
 */
public abstract class VitalCountdownMinigameState implements VitalMinigameState, RequiresAnnotation<VitalCountdownTaskInfo> {
    // error can be ignored since the implementation of this class will always be a component
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaPlugin plugin;
    private VitalCountdownTask.Spigot vitalCountdownTask;

    /**
     * Constructor for VitalCountdownMinigameState with the default interval from the annotation.
     */
    public VitalCountdownMinigameState() {
        final VitalCountdownTaskInfo vitalCountdownTaskInfo = getRequiredAnnotation();

        run(vitalCountdownTaskInfo.interval(), vitalCountdownTaskInfo.value());
    }

    /**
     * Constructor for VitalCountdownMinigameState with custom interval and countdown values.
     *
     * @param interval  The countdown update interval.
     * @param countdown The initial countdown value.
     */
    public VitalCountdownMinigameState(int interval, int countdown) {
        run(interval, countdown);
    }

    /**
     * Gets the {@link JavaPlugin} instance attached to this {@link VitalCountdownMinigameState}.
     *
     * @return The {@link JavaPlugin} instance attached to this {@link VitalCountdownMinigameState}.
     */
    public JavaPlugin getJavaPlugin() {
        return vitalCountdownTask.getPlugin();
    }

    /**
     * Sets up the countdown task using a VitalRepeatableTask.
     *
     * @param countdown The countdown.
     * @param interval  The countdown update interval.
     */
    private void run(int interval, int countdown) {
        vitalCountdownTask = new VitalCountdownTask.Spigot(plugin, interval, countdown) {
            @Override
            public void onStart() {
                VitalCountdownMinigameState.this.onCountdownStart();
            }

            @Override
            public void onTick() {
                VitalCountdownMinigameState.this.onCountdownTick();
            }

            @Override
            public void onStop() {
                VitalCountdownMinigameState.this.onCountdownStop();
            }

            @Override
            public void onExpire() {
                VitalCountdownMinigameState.this.onCountdownExpire();
            }

            @Override
            public void onReset() {
                VitalCountdownMinigameState.this.onCountdownReset();
            }

            @Override
            public void onRestart() {
                VitalCountdownMinigameState.this.onCountdownRestart();
            }
        };
    }

    /**
     * Start the countdown.
     */
    public final void startCountdown() {
        vitalCountdownTask.start();
    }

    /**
     * Stop the countdown.
     */
    public final void stopCountdown() {
        vitalCountdownTask.stop();
    }

    /**
     * Reset the countdown to its initial value.
     */
    public final void resetCountdown() {
        vitalCountdownTask.reset();
    }

    /**
     * Reset the countdown to its initial value, restarting its responsible {@link VitalRepeatableTask}.
     */
    public final void restartCountdown() {
        vitalCountdownTask.restart();
    }

    /**
     * Called when the countdown starts.
     */
    public void onCountdownStart() {

    }

    /**
     * Called on each countdown tick.
     */
    public void onCountdownTick() {

    }

    /**
     * Called when the countdown stops.
     */
    public void onCountdownStop() {

    }

    /**
     * Called when the countdown expires.
     */
    public void onCountdownExpire() {

    }

    /**
     * Called when the countdown is told to reset to its initial value.
     */
    public void onCountdownReset() {

    }

    /**
     * Called when the countdown is told to restart.
     *
     * @see VitalCountdownMinigameState#restartCountdown()
     */
    public void onCountdownRestart() {

    }

    @Override
    public Class<VitalCountdownTaskInfo> requiredAnnotationType() {
        return VitalCountdownTaskInfo.class;
    }

    /**
     * Gets the initial countdown of this minigame state.
     *
     * @return The initial countdown.
     */
    public int getInitialCountdown() {
        return vitalCountdownTask.getInitialCountdown();
    }

    /**
     * Gets the countdown of this minigame state.
     *
     * @return The current countdown.
     */
    public int getCountdown() {
        return vitalCountdownTask.getCountdown();
    }

    /**
     * Sets the current countdown of this minigame state.
     *
     * @param countdown The countdown.
     */
    public void setCountdown(int countdown) {
        vitalCountdownTask.setCountdown(countdown);
    }
}