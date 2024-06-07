package me.xra1ny.vital.logs;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * defines the vital-logs submodule
 */
@Component
public class VitalLogsSubModule extends VitalSubModule {
    @Override
    public @NotNull String getName() {
        return "vital-logs";
    }
}
