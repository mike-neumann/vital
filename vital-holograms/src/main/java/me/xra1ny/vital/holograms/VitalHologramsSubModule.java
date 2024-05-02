package me.xra1ny.vital.holograms;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-holograms submodule.
 */
@Component
public class VitalHologramsSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-holograms";
    }
}