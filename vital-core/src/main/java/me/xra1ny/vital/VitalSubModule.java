package me.xra1ny.vital;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Defines the vital-core submodule.
 */
@Slf4j
public abstract class VitalSubModule {
    @PostConstruct
    public final void init() {
        log.info("Using %s"
                .formatted(getClass().getSimpleName()));
    }
}