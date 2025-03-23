package me.vitalframework

import jakarta.annotation.PostConstruct

abstract class VitalSubModule {
    private val log = logger()

    @PostConstruct
    fun init() = log.info("Using {}", javaClass.getSimpleName())
}