package me.vitalframework.configs

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.constructor.Constructor

typealias SnakeYamlConstructor = Constructor

/**
 * Defines the official vital-configs submodule, which is displayed when Vital starts.
 * It contains the Vital configs system, which can be used to create class-based configs.
 */
@SubModule("vital-configs")
class VitalConfigsSubModule(
    val vitalConfigs: List<VitalConfig>,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        for (vitalConfig in vitalConfigs) {
            logger.info("Config '${vitalConfig::class.java.name}' successfully registered")
        }
    }
}
