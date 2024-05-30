package me.xra1ny.vital.items;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-items submodule.
 */
@Component
public class VitalItemsSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-items";
    }
}