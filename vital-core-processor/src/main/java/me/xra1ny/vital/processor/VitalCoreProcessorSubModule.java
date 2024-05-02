package me.xra1ny.vital.processor;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-core-processor submodule.
 */
@Component
public class VitalCoreProcessorSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-core-processor";
    }
}