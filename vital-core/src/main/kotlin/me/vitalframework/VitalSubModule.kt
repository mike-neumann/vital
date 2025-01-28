package me.vitalframework

import jakarta.annotation.PostConstruct
import me.vitalframework.Vital.logger

abstract class VitalSubModule {
    private val log = logger()

    @PostConstruct
    fun init() {
        log.info("Using {}", javaClass.getSimpleName())
    }
}