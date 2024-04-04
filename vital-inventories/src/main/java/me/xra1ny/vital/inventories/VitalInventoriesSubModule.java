package me.xra1ny.vital.inventories;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

@Component
public class VitalInventoriesSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-inventories";
    }
}