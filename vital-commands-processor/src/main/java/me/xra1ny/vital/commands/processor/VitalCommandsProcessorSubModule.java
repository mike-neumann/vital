package me.xra1ny.vital.commands.processor;

import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.VitalSubModule;
import org.jetbrains.annotations.NotNull;

/**
 * Defines the vital-commands-processor submodule.
 */
@Component
public class VitalCommandsProcessorSubModule extends VitalSubModule {
    @Override
    @NotNull
    public String getName() {
        return "vital-commands-processor";
    }
}