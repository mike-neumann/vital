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

        try {
            logger.info("Installing Vital submodule '$vitalSubModuleName'...")
            onInstall()
            logger.info("Vital submodule '$vitalSubModuleName' successfully installed")
        } catch (e: Exception) {
            logger.error("Error while installing Vital submodule '$vitalSubModuleName'", e)
        }
    }

    /**
     * Invoked as part of the installation process for a Vital submodule.
     *
     * This method is called after the submodule is registered with the Vital framework, providing
     * a hook for submodules to perform further initialization or configuration tasks during their
     * setup phase. Subclasses can override this method to define module-specific behavior during
     * the installation phase.
     *
     * Ensure that this method does not perform any blocking or long-running operations, as it is
     * invoked during the initialization sequence of the Vital framework.
     */
    fun onInstall() {
    }
}
