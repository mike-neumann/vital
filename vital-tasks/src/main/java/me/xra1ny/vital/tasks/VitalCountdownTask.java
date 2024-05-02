package me.xra1ny.vital.tasks;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.xra1ny.vital.AnnotatedVitalComponent;
import me.xra1ny.vital.tasks.annotation.VitalCountdownTaskInfo;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public abstract class VitalCountdownTask<Plugin, Runnable extends java.lang.Runnable, Task> implements AnnotatedVitalComponent<VitalCountdownTaskInfo> {
    @Getter
    private final int initialCountdown;

    private VitalRepeatableTask<Plugin, Runnable, Task> vitalRepeatableTask;

    @Getter
    @Setter
    private int countdown;

    @Getter
    @Setter
    private int interval;

    public VitalCountdownTask(@NonNull Plugin plugin) {
        final VitalCountdownTaskInfo vitalCountdownTaskInfo = getRequiredAnnotation();

        initialCountdown = vitalCountdownTaskInfo.value();
        countdown = initialCountdown;

        run(plugin);
    }

    public VitalCountdownTask(@NonNull Plugin plugin, int interval, int countdown) {
        initialCountdown = countdown;
        this.countdown = initialCountdown;
        this.interval = interval;

        run(plugin);
    }

    @Override
    public Class<VitalCountdownTaskInfo> requiredAnnotationType() {
        return VitalCountdownTaskInfo.class;
    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onUnregistered() {

    }

    @NonNull
    public Plugin getPlugin() {
        return vitalRepeatableTask.getPlugin();
    }

    /**
     * Sets up the countdown task using a VitalRepeatableTask.
     */
    private void run(@NonNull Plugin plugin) {
        vitalRepeatableTask = createVitalRepeatableTask(plugin);
    }

    protected abstract VitalRepeatableTask<Plugin, Runnable, Task> createVitalRepeatableTask(@NonNull Plugin plugin);

    /**
     * Starts the countdown.
     */
    public final void start() {
        vitalRepeatableTask.start();
    }

    /**
     * Stops the countdown.
     */
    public final void stop() {
        vitalRepeatableTask.stop();
    }

    /**
     * Resets the countdown to its initial value.
     */
    public final void reset() {
        countdown = initialCountdown;
        onReset();
    }

    /**
     * Resets the countdown to its initial value, restarting its responsible {@link VitalRepeatableTask}.
     */
    public final void restart() {
        vitalRepeatableTask.stop();
        reset();
        vitalRepeatableTask.start();
        onRestart();
    }

    /**
     * Called when the countdown starts.
     */
    public void onStart() {

    }

    /**
     * Called on each countdown tick.
     */
    public void onTick() {

    }

    /**
     * Called when the countdown stops.
     */
    public void onStop() {

    }

    /**
     * Called when the countdown expires.
     */
    public void onExpire() {

    }

    /**
     * Called when the countdown is told to reset to its initial value.
     */
    public void onReset() {

    }

    /**
     * Called when the countdown is told to restart.
     *
     * @see VitalCountdownTask#restart()
     */
    public void onRestart() {

    }

    /**
     * The spigot implementation for any vital countdown task.
     */
    public static class Spigot extends VitalCountdownTask<JavaPlugin, BukkitRunnable, BukkitTask> {
        /**
         * Constructs a new spigot impl for vital countdown task.
         *
         * @param javaPlugin The spigot plugin impl.
         */
        public Spigot(@NonNull JavaPlugin javaPlugin) {
            super(javaPlugin);
        }

        public Spigot(@NonNull JavaPlugin javaPlugin, int interval, int countdown) {
            super(javaPlugin, interval, countdown);
        }

        @Override
        protected VitalRepeatableTask<JavaPlugin, BukkitRunnable, BukkitTask> createVitalRepeatableTask(@NonNull JavaPlugin plugin) {
            return new VitalRepeatableTask.Spigot(plugin, getInterval()) {
                @Override
                public void onStart() {
                    VitalCountdownTask.Spigot.this.onStart();
                }

                @Override
                public void onStop() {
                    VitalCountdownTask.Spigot.this.onStop();
                }

                @Override
                public void onTick() {
                    if (getCountdown() <= 0) {
                        stop();
                        onExpire();

                        return;
                    }

                    VitalCountdownTask.Spigot.this.onTick();

                    setCountdown(getCountdown() - 1);
                }
            };
        }
    }

    /**
     * The bungeecord implementation for any vital countdown task.
     */
    public static class Bungeecord extends VitalCountdownTask<net.md_5.bungee.api.plugin.Plugin, java.lang.Runnable, ScheduledTask> {
        /**
         * Constructs a new bungeecord impl for vital countdown task.
         *
         * @param plugin The bungeecord plugin impl.
         */
        public Bungeecord(@NonNull net.md_5.bungee.api.plugin.Plugin plugin) {
            super(plugin);
        }

        public Bungeecord(@NonNull net.md_5.bungee.api.plugin.Plugin plugin, int interval, int countdown) {
            super(plugin, interval, countdown);
        }

        @Override
        protected VitalRepeatableTask<net.md_5.bungee.api.plugin.Plugin, java.lang.Runnable, ScheduledTask> createVitalRepeatableTask(@NotNull net.md_5.bungee.api.plugin.Plugin plugin) {
            return new VitalRepeatableTask.Bungeecord(plugin, getInterval()) {
                @Override
                public void onStart() {
                    VitalCountdownTask.Bungeecord.this.onStart();
                }

                @Override
                public void onStop() {
                    VitalCountdownTask.Bungeecord.this.onStop();
                }

                @Override
                public void onTick() {
                    if (getCountdown() <= 0) {
                        stop();
                        onExpire();

                        return;
                    }

                    VitalCountdownTask.Bungeecord.this.onTick();

                    setCountdown(getCountdown() - 1);
                }
            };
        }
    }
}