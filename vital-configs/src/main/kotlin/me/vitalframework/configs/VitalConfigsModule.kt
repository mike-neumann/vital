package me.vitalframework.configs

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
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
