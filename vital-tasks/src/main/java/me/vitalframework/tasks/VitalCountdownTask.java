package me.vitalframework.tasks;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.vitalframework.RequiresAnnotation;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class VitalCountdownTask<P, T extends VitalRepeatableTask<?, ?, ?>> implements RequiresAnnotation<VitalCountdownTask.Info> {
    @Getter
    private final int initialCountdown;

    @Getter
    @Autowired
    private P plugin;

    @Getter
    @Setter
    private int countdown;

    @Getter
    @Setter
    private int interval;

    private T vitalRepeatableTask;

    /**
     * Constructor for when using dependency injection
     */
    public VitalCountdownTask() {
        final var vitalCountdownTaskInfo = getRequiredAnnotation();

        initialCountdown = vitalCountdownTaskInfo.countdown();
        countdown = initialCountdown;
        interval = vitalCountdownTaskInfo.interval();
    }

    /**
     * Constructor for when not using dependency injection
     */
    public VitalCountdownTask(@NonNull P plugin) {
        final var vitalCountdownTaskInfo = getRequiredAnnotation();

        this.plugin = plugin;
        initialCountdown = vitalCountdownTaskInfo.countdown();
        countdown = initialCountdown;
        interval = vitalCountdownTaskInfo.interval();
    }

    @PostConstruct
    public void init() {
        run();
    }

    @Override
    public final Class<Info> requiredAnnotationType() {
        return Info.class;
    }

    /**
     * Sets up the countdown task using a VitalRepeatableTask.
     */
    private void run() {
        vitalRepeatableTask = createVitalRepeatableTask();
    }

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

    public final void handleTick() {
        if (getCountdown() <= 0) {
            stop();
            onExpire();

            return;
        }

        onTick();
        setCountdown(getCountdown() - 1);
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

    protected abstract T createVitalRepeatableTask();

    @Component
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        /**
         * Defines the countdown for this annotated countdown task in seconds.
         *
         * @return The countdown in seconds
         */
        int countdown();

        /**
         * Defines the interval between countdown task ticks in milliseconds.
         *
         * @return The interval in milliseconds.
         */
        int interval() default 1_000;
    }

    public static abstract class Spigot extends VitalCountdownTask<JavaPlugin, VitalRepeatableTask.Spigot> {
        public Spigot() {
        }

        public Spigot(JavaPlugin plugin) {
            super(plugin);
        }

        @Override
        protected VitalRepeatableTask.Spigot createVitalRepeatableTask() {
            return new VitalRepeatableTask.Spigot(getPlugin(), getInterval()) {
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
                    handleTick();
                }
            };
        }
    }

    public static abstract class Bungeecord extends VitalCountdownTask<Plugin, VitalRepeatableTask.Bungeecord> {
        public Bungeecord() {
        }

        public Bungeecord(Plugin plugin) {
            super(plugin);
        }

        @Override
        protected VitalRepeatableTask.Bungeecord createVitalRepeatableTask() {
            return new VitalRepeatableTask.Bungeecord(getPlugin(), getInterval()) {
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
                    handleTick();
                }
            };
        }
    }
}