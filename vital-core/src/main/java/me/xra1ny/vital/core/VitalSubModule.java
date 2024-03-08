package me.xra1ny.vital.core;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;

@Log
public abstract class VitalSubModule implements VitalComponent {
    @Override
    @NotNull
    public abstract String getName();

    @Override
    public final void onRegistered() {
        log.info("Using %s"
                .formatted(getName()));
    }

    @Override
    public final void onUnregistered() {

    }
}
