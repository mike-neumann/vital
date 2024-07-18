package me.xra1ny.vital;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;

/**
 * Defines the vital-core submodule.
 */
@Log
public abstract class VitalSubModule {
    @PostConstruct
    public final void init() {
        log.info("Using %s"
                .formatted(getClass().getSimpleName()));
    }
}