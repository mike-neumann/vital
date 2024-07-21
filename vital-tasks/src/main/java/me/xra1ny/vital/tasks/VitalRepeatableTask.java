package me.xra1ny.vital.tasks;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.xra1ny.vital.RequiresAnnotation;
import me.xra1ny.vital.tasks.annotation.VitalRepeatableTaskInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for creating repeatable tasks in the Vital plugin framework.
 * Repeatable tasks can be used to execute specific logic at defined intervals.
 *
 * @author xRa1ny
 */
@Component
public abstract class VitalRepeatableTask<P, R extends Runnable, T> implements RequiresAnnotation<VitalRepeatableTaskInfo> {
    @Autowired
    @Getter
    private P plugin;

    @Getter
    private R runnable;

    @Getter
    private T task;

    @Getter
    @Setter
    private int interval;

    @Getter
    @Setter
    private boolean allowTick = true;

    /**
     * Constructor for when using dependency injection
     */
    public VitalRepeatableTask() {
        final VitalRepeatableTaskInfo vitalRepeatableTaskInfo = getRequiredAnnotation();

        interval = vitalRepeatableTaskInfo.interval();
    }

    /**
     * Constructor for when not using dependency injection
     */
    public VitalRepeatableTask(@NonNull P plugin) {
        final VitalRepeatableTaskInfo vitalRepeatableTaskInfo = getRequiredAnnotation();

        this.plugin = plugin;
        interval = vitalRepeatableTaskInfo.interval();
    }

    /**
     * Constructor for when not using dependency injection
     */
    public VitalRepeatableTask(@NonNull P plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public final Class<VitalRepeatableTaskInfo> requiredAnnotationType() {
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

    /**
     * Checks if this repeatable task is currently running.
     *
     * @return True if the task is running, false otherwise.
     */
    public final boolean isRunning() {
        return runnable != null && task != null;
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

    /**
     * Called when this repeatable task starts.
     */
    public void onStart() {

    }

    /**
     * Called whenever the interval of this repeatable task expires.
     */
    public void onTick() {

    }

    /**
     * Called when this repeatable task stops.
     */
    public void onStop() {

    }

    protected abstract R createRunnable();

    protected abstract T createTask();

    protected abstract void cancelRunnable();

    protected abstract void cancelTask();

    @Component
    public static class Spigot extends VitalRepeatableTask<JavaPlugin, BukkitRunnable, BukkitTask> {
        public Spigot() {
        }

        public Spigot(@NonNull JavaPlugin plugin) {
            super(plugin);
        }

        public Spigot(@NonNull JavaPlugin plugin, int interval) {
            super(plugin, interval);
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

    @Component
    public static class Bungeecord extends VitalRepeatableTask<Plugin, Runnable, ScheduledTask> {
        public Bungeecord() {
        }

        public Bungeecord(@NonNull Plugin plugin) {
            super(plugin);
        }

        public Bungeecord(@NonNull Plugin plugin, int interval) {
            super(plugin, interval);
        }

        @Override
        protected Runnable createRunnable() {
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