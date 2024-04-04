package me.xra1ny.vital;

import me.xra1ny.essentia.inject.annotation.Component;
import org.jetbrains.annotations.NotNull;

@Component
public class VitalCoreSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-core";
    }
}