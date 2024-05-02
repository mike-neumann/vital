package me.xra1ny.vital.minigames;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-minigames submodule.
 */
@Component
public class VitalMinigameSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-minigames";
    }
}