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
    private val logger = logger<VitalSubModule>()

    final override fun afterPropertiesSet() {
        val vitalSubModuleName = getRequiredAnnotation<Component>().value
        logger.info("Using '$vitalSubModuleName'")
        Vital.vitalSubModules.add(vitalSubModuleName)
    }
}
