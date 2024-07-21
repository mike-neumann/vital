package me.xra1ny.vital;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Defines the vital-core submodule.
 */
@Slf4j
@Component
public abstract class VitalSubModule {
    @PostConstruct
    public final void init() {
        log.info("Using %s"
                .formatted(getClass().getSimpleName()));
    }
}