package me.vitalframework

import me.vitalframework.VitalCoreModule.Companion.getRequiredAnnotation
import me.vitalframework.VitalCoreModule.Companion.logger
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AliasFor

/**
 * A module includes additional features within the Vital ecosystem.
 * Every module is essentially a [Configuration] and should define every module bean.
 * Additionally, a module can also display what features are added, etc.
 *
 * ```java
 * @VitalModule.Info("mymodule")
 * public class MyModule extends VitalModule {
 *     @Override
 *     public void onEnable() {
 *         // ...
 *     }
 *
 *     @Override
 *     public void onDisable() {
 *         // ...
 *     }
 * }
 * ```
 */
abstract class VitalModule : VitalHasInfo {
    override val info = mutableMapOf(Info::class.java to javaClass.getRequiredAnnotation<Info>())

    private val logger = logger()

    /**
     * Enables this module by calling the [onEnable] lifecycle function.
     * The module is only enabled if it is not already enabled.
     */
    fun enable() {
        val info = getInfo(Info::class.java)
        try {
            if (Vital.isVitalModuleEnabled(info.value)) {
                logger.error("Cannot enable Vital module '${info.value}', it is already enabled.")
                return
            }

            if (info.value.startsWith("vital-") && !Vital.isOfficialVitalModule(info.value)) {
                logger.error("!!! '${info.value}' is not an official Vital module but is trying to disguise itself as one !!!")
                logger.error("!!! This does not mean it is trying to cause harm. !!!")
                logger.error("!!! If you are the developer of '${info.value}', please consider renaming your module. !!!")
                logger.error("!!! Only official Vital modules should start with 'vital-'!!!")
            }

            logger.info("Enabling Vital module '${info.value}'.")
            onEnable()
            Vital.vitalModules.add(info.value)
            logger.info("Vital module '${info.value}' successfully enabled.")
        } catch (e: Exception) {
            logger.error("Error while enabling Vital module '${info.value}'", e)
        }
    }

    /**
     * Disables this module by calling the [onDisable] lifecycle function.
     * The module is only disabled if it is currently enabled.
     */
    fun disable() {
        val info = getInfo(Info::class.java)
        try {
            if (!Vital.isVitalModuleEnabled(info.value)) {
                logger.error("Cannot disable Vital module '${info.value}', it is already disabled.")
                return
            }

            logger.info("Disabling Vital module '${info.value}'.")
            onDisable()
            Vital.vitalModules.remove(info.value)
            logger.info("Vital module '${info.value}' successfully disabled.")
        } catch (e: Exception) {
            logger.error("Error while disabling Vital module '${info.value}'.", e)
        }
    }

    /**
     * Lifecycle function; called when this module is enabled.
     * Override this function to for example perform initialization logic for your module.
     */
    open fun onEnable() {
    }

    /**
     * Lifecycle function; called when this module is disabled.
     * Override this function to for example perform teardown logic for your module.
     */
    open fun onDisable() {
    }

    /**
     * Defines the info of a [VitalModule].
     * This annotation must be applied to all implementations of [VitalModule].
     *
     * ```java
     * @VitalModule.Info("mymodule")
     * public class MyModule extends VitalModule {
     * }
     * ```
     */
    @Configuration
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        /**
         * The name of the module, e.g. `mymodule`, `mymodule.spigot`, `mymodule.bungee`, etc.
         */
        @get:AliasFor(annotation = Configuration::class)
        val value: String,
    )
}
