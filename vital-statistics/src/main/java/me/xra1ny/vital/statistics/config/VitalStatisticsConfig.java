package me.xra1ny.vital.statistics.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class VitalStatisticsConfig {
    /**
     * The minimum tps for vital to detect a "HEALTHY" server
     * Default: 16
     */
    @Value("${plugin.min-tps:16}")
    private int minTps;

    /**
     * The maximum allowed task inactive time measured in millis for vital to detect any timer inconsistencies
     * Default: 250
     */
    @Value("${plugin.max-task-inactive-tolerance:250}")
    private int maxTaskInactiveTolerance;

    /**
     * The amount of tps task reports to cache before deleting older ones
     * Default: 16
     */
    @Value("${plugin.max-tps-task-cache:16}")
    private int maxTpsTaskCache;
}
