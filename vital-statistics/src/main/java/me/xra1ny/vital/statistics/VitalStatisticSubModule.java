package me.xra1ny.vital.statistics;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-statistics submodule.
 */
@Component
public class VitalStatisticSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-statistics";
    }
}