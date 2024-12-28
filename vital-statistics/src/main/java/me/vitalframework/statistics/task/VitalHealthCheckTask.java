package me.vitalframework.statistics.task;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.vitalframework.RequiresBungee;
import me.vitalframework.RequiresSpigot;
import me.vitalframework.statistics.config.VitalStatisticsConfig;
import me.vitalframework.tasks.VitalRepeatableTask;
import net.md_5.bungee.api.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public interface VitalHealthCheckTask {
    @NonNull
    Logger log = LoggerFactory.getLogger(VitalHealthCheckTask.class);

    long getLastTickTime();

    void setLastTickTime(long tickTime);

    long getLastSecondTime();

    void setLastSecondTime(long secondTime);

    int getTicks();

    void setTicks(int ticks);

    /**
     * The last reported tps of this healthcheck cycle
     *
     * @return The last tps report
     */
    int getTps();

    void setTps(int tps);

    /**
     * Gets all tps reports this healthcheck task has committed
     *
     * @return All tps reports of this healthcheck implementation
     */
    @NonNull
    Map<Long, Integer> getLastTps();

    /**
     * gets all last reported unhealthy tps this task has committed
     *
     * @return All unhealthy tps of this healthcheck implementation
     */
    @NonNull
    Map<Long, Integer> getLastUnhealthyTps();

    @NonNull
    VitalStatisticsConfig getVitalStatisticsConfig();

    default void handleTick() {
        final var currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis - getLastTickTime() >= getVitalStatisticsConfig().getMaxTaskInactiveTolerance()) {
            // inconsistent ticks
            log.warn("vital-statistics has detected increased scheduler inconsistency of {} millis", currentTimeMillis - getLastTickTime());
            log.warn("This could indicate bad server-performance / health");
        }

        if (currentTimeMillis - getLastSecondTime() >= 1_000) {
            // one second has passed
            setLastSecondTime(System.currentTimeMillis());
            setTps(getTicks());
            setTicks(0);
            getLastTps().put(getLastSecondTime(), getTps());

            if (getLastTps().size() > getVitalStatisticsConfig().getMaxTpsTaskCache()) {
                removeFirst(this::getLastTps);
            }

            if (getTps() < getVitalStatisticsConfig().getMinTps()) {
                getLastUnhealthyTps().put(System.currentTimeMillis(), getTps());

                if (getLastUnhealthyTps().size() > getVitalStatisticsConfig().getMaxTpsTaskCache()) {
                    removeFirst(this::getLastUnhealthyTps);
                }
            }
        }

        setLastTickTime(System.currentTimeMillis());
        setTicks(getTicks() + 1);
    }

    default <K, V> void removeFirst(@NonNull Supplier<Map<K, V>> map) {
        final var cache = new ArrayList<>(map.get().entrySet().stream().toList());

        map.get().clear();
        cache.removeFirst();

        for (var entry : cache) {
            map.get().put(entry.getKey(), entry.getValue());
        }
    }

    @Getter
    @Setter
    @RequiresSpigot
    @VitalRepeatableTask.Info(interval = 50)
    class Spigot extends VitalRepeatableTask.Spigot implements VitalHealthCheckTask {
        @NonNull
        private final Map<Long, Integer> lastTps = new HashMap<>();

        @NonNull
        private final Map<Long, Integer> lastUnhealthyTps = new HashMap<>();

        @NonNull
        private final VitalStatisticsConfig vitalStatisticsConfig;

        private long lastTickTime = System.currentTimeMillis();
        private long lastSecondTime = System.currentTimeMillis();
        private int ticks;
        private int tps;

        public Spigot(@NonNull JavaPlugin plugin, @NonNull VitalStatisticsConfig vitalStatisticsConfig) {
            super(plugin);
            this.vitalStatisticsConfig = vitalStatisticsConfig;
        }

        @PostConstruct
        public void init() {
            start();
        }

        @Override
        public void onTick() {
            handleTick();
        }
    }

    @Getter
    @Setter
    @RequiresBungee
    @VitalRepeatableTask.Info(interval = 50)
    class Bungeecord extends VitalRepeatableTask.Bungeecord implements VitalHealthCheckTask {
        @NonNull
        private final Map<Long, Integer> lastTps = new HashMap<>();

        @NonNull
        private final Map<Long, Integer> lastUnhealthyTps = new HashMap<>();

        @NonNull
        private final VitalStatisticsConfig vitalStatisticsConfig;

        private long lastTickTime = System.currentTimeMillis();
        private long lastSecondTime = System.currentTimeMillis();
        private int ticks;
        private int tps;

        public Bungeecord(@NonNull Plugin plugin, @NonNull VitalStatisticsConfig vitalStatisticsConfig) {
            super(plugin);
            this.vitalStatisticsConfig = vitalStatisticsConfig;
        }

        @PostConstruct
        public void init() {
            start();
        }

        @Override
        public void onTick() {
            handleTick();
        }
    }
}