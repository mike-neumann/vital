package me.vitalframework.configs

import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.constructor.Constructor

typealias SnakeYamlConstructor = Constructor

@Component("vital-configs")
class VitalConfigsSubModule : VitalSubModule()
