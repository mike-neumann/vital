package me.vitalframework

import jakarta.annotation.PostConstruct

/**
 * Defines a valid vital submodule.
 * Will be displayed when vital starts, to show what extra functionality will be available.
 */
abstract class VitalSubModule {
    private val log = logger()

    @PostConstruct
    fun init() = log.info("Using {}", javaClass.getSimpleName())
}