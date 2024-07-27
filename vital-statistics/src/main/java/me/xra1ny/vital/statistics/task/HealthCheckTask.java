package me.xra1ny.vital.statistics.task;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import me.xra1ny.vital.annotation.RequiresBungeecord;
import me.xra1ny.vital.annotation.RequiresSpigot;
import me.xra1ny.vital.statistics.config.VitalStatisticsConfig;
import me.xra1ny.vital.tasks.VitalRepeatableTask;
import me.xra1ny.vital.tasks.annotation.VitalRepeatableTaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public interface HealthCheckTask {
    Logger log = LoggerFactory.getLogger(HealthCheckTask.class);

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
    Map<Long, Integer> getLastTps();

    /**
     * gets all last reported unhealthy tps this task has committed
     *
     * @return All unhealthy tps of this healthcheck implementation
     */
    Map<Long, Integer> getLastUnhealthyTps();

    VitalStatisticsConfig getVitalStatisticsConfig();

    default void handleTick() {
        final long currentTimeMillis = System.currentTimeMillis();

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

    default <K, V> void removeFirst(Supplier<Map<K, V>> map) {
        final List<Map.Entry<K, V>> cache = new ArrayList<>(map.get().entrySet().stream().toList());

        map.get().clear();
        cache.removeFirst();

        for (Map.Entry<K, V> entry : cache) {
            map.get().put(entry.getKey(), entry.getValue());
        }
    }

    @Getter
    @Setter
    @RequiresSpigot
    @VitalRepeatableTaskInfo(interval = 50)
    class Spigot extends VitalRepeatableTask.Spigot implements HealthCheckTask {
        private long lastTickTime = System.currentTimeMillis();
        private long lastSecondTime = System.currentTimeMillis();
        private int ticks;
        private int tps;
        private final Map<Long, Integer> lastTps = new HashMap<>();
        private final Map<Long, Integer> lastUnhealthyTps = new HashMap<>();
        private final VitalStatisticsConfig vitalStatisticsConfig;

        public Spigot(VitalStatisticsConfig vitalStatisticsConfig) {
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
    @RequiresBungeecord
    @VitalRepeatableTaskInfo(interval = 50)
    class Bungeecord extends VitalRepeatableTask.Bungeecord implements HealthCheckTask {
        private long lastTickTime = System.currentTimeMillis();
        private long lastSecondTime = System.currentTimeMillis();
        private int ticks;
        private int tps;
        private final Map<Long, Integer> lastTps = new HashMap<>();
        private final Map<Long, Integer> lastUnhealthyTps = new HashMap<>();
        private final VitalStatisticsConfig vitalStatisticsConfig;

        public Bungeecord(VitalStatisticsConfig vitalStatisticsConfig) {
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
