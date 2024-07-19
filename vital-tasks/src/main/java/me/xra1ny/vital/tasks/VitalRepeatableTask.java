package me.xra1ny.vital.tasks;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.xra1ny.vital.RequiresAnnotation;
import me.xra1ny.vital.tasks.annotation.VitalRepeatableTaskInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for creating repeatable tasks in the Vital plugin framework.
 * Repeatable tasks can be used to execute specific logic at defined intervals.
 *
 * @author xRa1ny
 */
public abstract class VitalRepeatableTask<Plugin, Runnable extends java.lang.Runnable, Task> implements RequiresAnnotation<VitalRepeatableTaskInfo> {
    @Getter
    @NonNull
    @Autowired
    private Plugin plugin;

    /**
     * The interval at which this repeatable task should execute, in milliseconds.
     */
    @Getter
    @Setter
    private int interval;
    /**
     * The runnable associated with this repeatable task, defining its logic.
     */
    @Getter
    @NonNull
    private Runnable runnable;
    /**
     * The task representing this repeatable task.
     */
    @Getter
    private Task task;
    /**
     * If true this repeatable tasks tick Method is called.
     * If false, skips tick Method call.
     */
    @Getter
    @Setter
    private boolean allowTick = true;

    /**
     * Constructor to use when using springs dependency injection pattern.
     */
    public VitalRepeatableTask() {
        final VitalRepeatableTaskInfo vitalRepeatableTaskInfo = getRequiredAnnotation();

        interval = vitalRepeatableTaskInfo.interval();
    }

    /**
     * Constructor to use when manually implementing this class
     *
     * @param plugin The JavaPlugin instance associated with this task.
     */
    public VitalRepeatableTask(@NonNull Plugin plugin) {
        this.plugin = plugin;

        final VitalRepeatableTaskInfo vitalRepeatableTaskInfo = getRequiredAnnotation();

        interval = vitalRepeatableTaskInfo.interval();
    }

    /**
     * Creates a new instance of VitalRepeatableTask with the specified JavaPlugin and interval.
     *
     * @param plugin   The JavaPlugin instance associated with this task.
     * @param interval The interval at which this task should execute, in milliseconds.
     */
    public VitalRepeatableTask(@NonNull Plugin plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    public VitalRepeatableTask(int interval) {
        this.interval = interval;
    }

    @Override
    public Class<VitalRepeatableTaskInfo> requiredAnnotationType() {
        return VitalRepeatableTaskInfo.class;
    }

    /**
     * Starts this repeatable task. If it's already running, this method has no effect.
     */
    public final void start() {
        if (isRunning()) {
            return;
        }

        onStart();
        runnable = createRunnable();
        task = createTask();
    }

    protected abstract Runnable createRunnable();

    protected abstract Task createTask();

    /**
     * Called when this repeatable task starts.
     */
    public void onStart() {

    }

    /**
     * Stops this repeatable task. If it's not running, this method has no effect.
     */
    public final void stop() {
        if (!isRunning()) {
            return;
        }

        onStop();
        cancelTask();
        cancelRunnable();
        task = null;
        runnable = null;
    }

    protected abstract void cancelRunnable();

    protected abstract void cancelTask();

    /**
     * Called when this repeatable task stops.
     */
    public void onStop() {

    }

    /**
     * Checks if this repeatable task is currently running.
     *
     * @return True if the task is running, false otherwise.
     */
    public final boolean isRunning() {
        return runnable != null && task != null;
    }

    /**
     * Called whenever the interval of this repeatable task expires.
     */
    public void onTick() {

    }

    /**
     * The spigot implementation for vital repeatable task.
     */
    public static class Spigot extends VitalRepeatableTask<JavaPlugin, BukkitRunnable, BukkitTask> {
        public Spigot() {
            super();
        }

        /**
         * Constructs a new spigot impl. for vital repeatable task.
         *
         * @param javaPlugin The spigot plugin impl.
         */
        public Spigot(@NonNull JavaPlugin javaPlugin) {
            super(javaPlugin);
        }

        /**
         * Constructs a new spigot impl. for vital repeatable task with the given interval.
         *
         * @param javaPlugin The spigot plugin impl.
         * @param interval
         */
        public Spigot(@NonNull JavaPlugin javaPlugin, int interval) {
            super(javaPlugin, interval);
        }

        public Spigot(int interval) {
            super(interval);
        }

        @Override
        protected BukkitRunnable createRunnable() {
            return new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isAllowTick()) {
                        return;
                    }

                    onTick();
                }
            };
        }

        @Override
        protected BukkitTask createTask() {
            return getRunnable().runTaskTimer(getPlugin(), 0L, (long) ((getInterval() / 1000D) * 20L));
        }

        @Override
        protected void cancelRunnable() {
            getRunnable().cancel();
        }

        @Override
        protected void cancelTask() {
            getTask().cancel();
        }
    }

    /**
     * The bungeecord implementation for vital repeatable task.
     */
    public static class Bungeecord extends VitalRepeatableTask<net.md_5.bungee.api.plugin.Plugin, java.lang.Runnable, ScheduledTask> {
        public Bungeecord() {
            super();
        }

        /**
         * Constructs a new bungeecord impl. for vital repeatable task.
         *
         * @param plugin The bungeecord plugin impl.
         */
        public Bungeecord(net.md_5.bungee.api.plugin.@NonNull Plugin plugin) {
            super(plugin);
        }

        /**
         * Constructs a new bungeecord impl. for vital repeatable task with the given interval.
         *
         * @param plugin   The bungeecord plugin impl.
         * @param interval The interval.
         */
        public Bungeecord(net.md_5.bungee.api.plugin.@NonNull Plugin plugin, int interval) {
            super(plugin, interval);
        }

        public Bungeecord(int interval) {
            super(interval);
        }

        @Override
        protected java.lang.Runnable createRunnable() {
            return () -> {
                if (!isAllowTick()) {
                    return;
                }

                onTick();
            };
        }

        @Override
        protected ScheduledTask createTask() {
            return ProxyServer.getInstance().getScheduler().schedule(getPlugin(), getRunnable(), 0L, getInterval(), TimeUnit.MILLISECONDS);
        }

        @Override
        protected void cancelRunnable() {
            getTask().cancel();
        }

        @Override
        protected void cancelTask() {
            getTask().cancel();
        }
    }
}