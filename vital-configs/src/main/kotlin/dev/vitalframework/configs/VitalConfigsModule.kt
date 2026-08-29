package dev.vitalframework.configs

import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import org.yaml.snakeyaml.constructor.Constructor

typealias SnakeYamlConstructor = Constructor

@VitalModule.Info(value = "vital-configs")
class VitalConfigsModule(
    val vitalConfigs: List<VitalConfig>,
) : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        for (vitalConfig in vitalConfigs) {
            logger.info("Config '${vitalConfig::class.java.name}' successfully registered")
        }
    }
}
