package me.vitalframework.tasks;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.vitalframework.RequiresAnnotation;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for creating repeatable tasks in the Vital plugin framework.
 * Repeatable tasks can be used to execute specific logic at defined intervals.
 *
 * @author xRa1ny
 */
@Getter
public abstract class VitalRepeatableTask<P, R extends Runnable, T> implements RequiresAnnotation<VitalRepeatableTask.Info> {
    @NonNull
    private final P plugin;

    private R runnable;
    private T task;

    @Setter
    private int interval;

    @Setter
    private boolean allowTick = true;

    public VitalRepeatableTask(@NonNull P plugin) {
        final var vitalRepeatableTaskInfo = getRequiredAnnotation();

        this.plugin = plugin;
        interval = vitalRepeatableTaskInfo.interval();
    }

    public VitalRepeatableTask(@NonNull P plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public final @NonNull Class<Info> requiredAnnotationType() {
        return Info.class;
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

    @NonNull
    protected abstract R createRunnable();

    @NonNull
    protected abstract T createTask();

    protected abstract void cancelRunnable();

    protected abstract void cancelTask();

    /**
     * Annotation used to provide information about the interval of a {@link VitalRepeatableTask}.
     *
     * @author xRa1ny
     */
    @Component
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Info {
        /**
         * Defines the interval at which the repeatable task should execute, in milliseconds.
         *
         * @return The interval for the repeatable task execution.
         */
        int interval();
    }

    public static abstract class Spigot extends VitalRepeatableTask<JavaPlugin, BukkitRunnable, BukkitTask> {
        public Spigot(@NonNull JavaPlugin plugin) {
            super(plugin);
        }

        public Spigot(@NonNull JavaPlugin plugin, int interval) {
            super(plugin, interval);
        }

        @Override
        protected @NonNull BukkitRunnable createRunnable() {
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
        protected @NonNull BukkitTask createTask() {
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

    public static abstract class Bungeecord extends VitalRepeatableTask<Plugin, Runnable, ScheduledTask> {
        public Bungeecord(@NonNull Plugin plugin) {
            super(plugin);
        }

        public Bungeecord(@NonNull Plugin plugin, int interval) {
            super(plugin, interval);
        }

        @Override
        protected @NonNull Runnable createRunnable() {
            return () -> {
                if (!isAllowTick()) {
                    return;
                }

                onTick();
            };
        }

        @Override
        protected @NonNull ScheduledTask createTask() {
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