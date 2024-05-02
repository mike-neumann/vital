package me.xra1ny.vital.players;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-players submodule.
 */
@Component
public class VitalPlayersSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-players";
    }
}