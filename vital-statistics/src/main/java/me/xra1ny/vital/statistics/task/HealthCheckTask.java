package me.xra1ny.vital.statistics.task;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
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

@Component
public interface HealthCheckTask {
    Logger log = LoggerFactory.getLogger(HealthCheckTask.class);

    /**
     * The last reported tps of this healthcheck cycle
     *
     * @return The last tps report
     */
    int getTps();

    /**
     * Gets all tps reports this healthcheck task has committed
     *
     * @return All tps reports of this healthcheck implementation
     */
    Map<Long, Integer> getLastTps();

    VitalStatisticsConfig getVitalStatisticsConfig();

    default void removeFirstTpsReport() {
        final List<Map.Entry<Long, Integer>> cache = new ArrayList<>(getLastTps().entrySet().stream().toList());

        getLastTps().clear();
        cache.removeFirst();

        for (Map.Entry<Long, Integer> longIntegerEntry : cache) {
            getLastTps().put(longIntegerEntry.getKey(), longIntegerEntry.getValue());
        }
    }

    @Getter
    @RequiresSpigot
    @VitalRepeatableTaskInfo(interval = 50)
    class Spigot extends VitalRepeatableTask.Spigot implements HealthCheckTask {
        private long lastTickTime = System.currentTimeMillis();
        private long lastSecondTime = System.currentTimeMillis();
        private int ticks;
        private int tps;
        private final Map<Long, Integer> lastTps = new HashMap<>();
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
            final long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis - lastTickTime >= getVitalStatisticsConfig().getMaxTaskInactiveTolerance()) {
                // inconsistent ticks
                log.warn("vital-statistics has detected increased scheduler inconsistency of {} millis", currentTimeMillis - lastTickTime);
                log.warn("This could indicate bad server-performance / health");
            }

            if (currentTimeMillis - lastSecondTime >= 1_000) {
                // one second has passed
                lastSecondTime = System.currentTimeMillis();
                tps = ticks;
                ticks = 0;
                lastTps.put(lastSecondTime, tps);
            }

            if (lastTps.size() > getVitalStatisticsConfig().getMaxTpsTaskCache()) {
                removeFirstTpsReport();
            }

            lastTickTime = System.currentTimeMillis();
            ticks++;
        }
    }

    @Getter
    @RequiresBungeecord
    @VitalRepeatableTaskInfo(interval = 50)
    class Bungeecord extends VitalRepeatableTask.Bungeecord implements HealthCheckTask {
        private long lastTickTime = System.currentTimeMillis();
        private long lastSecondTime = System.currentTimeMillis();
        private int ticks;
        private int tps;
        private final Map<Long, Integer> lastTps = new HashMap<>();
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
            final long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis - lastTickTime >= getVitalStatisticsConfig().getMaxTaskInactiveTolerance()) {
                // inconsistent ticks
                log.warn("vital-statistics has detected increased scheduler inconsistency of {} millis", currentTimeMillis - lastTickTime);
                log.warn("This could indicate bad server-performance / health");
            }

            if (currentTimeMillis - lastSecondTime >= 1_000) {
                // one second has passed
                lastSecondTime = System.currentTimeMillis();
                tps = ticks;
                ticks = 0;
                lastTps.put(lastSecondTime, tps);
            }

            if (lastTps.size() > getVitalStatisticsConfig().getMaxTpsTaskCache()) {
                removeFirstTpsReport();
            }

            ticks++;
            lastTickTime = System.currentTimeMillis();
        }
    }
}
