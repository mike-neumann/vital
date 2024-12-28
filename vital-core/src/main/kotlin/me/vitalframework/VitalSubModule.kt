package me.vitalframework

import jakarta.annotation.PostConstruct
import me.vitalframework.Vital.log

abstract class VitalSubModule {
    @PostConstruct
    fun init() {
        log().info("Using {}", javaClass.getSimpleName())
    }
}