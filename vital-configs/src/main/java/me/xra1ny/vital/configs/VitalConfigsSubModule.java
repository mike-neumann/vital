package me.xra1ny.vital.configs;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-configs submodule.
 */
@Component
public class VitalConfigsSubModule extends VitalSubModule {
    @Override
    public @NotNull String getName() {
        return "vital-configs";
    }
}