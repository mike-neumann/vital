package me.vitalframework

import me.vitalframework.VitalCoreSubModule.Companion.getRequiredAnnotation
import me.vitalframework.VitalCoreSubModule.Companion.logger
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component

/**
 * Defines a valid Vital submodule.
 * Will be displayed when Vital starts, to show what extra functionality will be available.
 */
@Component
abstract class VitalSubModule : InitializingBean {
    private val logger = logger()

    final override fun afterPropertiesSet() {
        val vitalSubModuleName = getRequiredAnnotation<Component>().value
        Vital.vitalSubModules.add(vitalSubModuleName)
        logger.info("Vital submodule '$vitalSubModuleName' installed")
    }
}
