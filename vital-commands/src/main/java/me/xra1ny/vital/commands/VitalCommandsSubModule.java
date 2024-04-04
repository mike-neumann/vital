package me.xra1ny.vital.commands;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

@Component
public class VitalCommandsSubModule extends VitalSubModule {
    @Override
    public @NotNull String getName() {
        return "vital-commands";
    }
}