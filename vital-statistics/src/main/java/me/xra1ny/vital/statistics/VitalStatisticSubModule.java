package me.xra1ny.vital.statistics;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.core.VitalSubModule;
import org.jetbrains.annotations.NotNull;

@Component
public class VitalStatisticSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-statistics";
    }
}
