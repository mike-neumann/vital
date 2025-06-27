package me.vitalframework

import jakarta.annotation.PostConstruct
import me.vitalframework.VitalClassUtils.getRequiredAnnotation
import org.springframework.stereotype.Component

/**
 * Defines a valid Vital submodule.
 * Will be displayed when Vital starts, to show what extra functionality will be available.
 */
@Component
abstract class VitalSubModule {
    private val logger = logger<VitalSubModule>()

    @PostConstruct
    fun init() {
        val vitalSubModuleName = getRequiredAnnotation<Component>().value
        logger.info("Using '$vitalSubModuleName'")
        Vital.vitalSubModules.add(vitalSubModuleName)
    }
}