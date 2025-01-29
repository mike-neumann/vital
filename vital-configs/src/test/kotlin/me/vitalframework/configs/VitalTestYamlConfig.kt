package me.vitalframework.configs

import me.vitalframework.configs.processor.VitalYAMLConfigProcessor

@VitalConfig.Info("test.yaml", VitalYAMLConfigProcessor::class)
class VitalTestYamlConfig : VitalConfig() {
    @Property(String::class)
    lateinit var testYamlString: String

    @Property(TestYamlObject::class)
    lateinit var testYamlObject: TestYamlObject

    class TestYamlObject {
        @Property(String::class)
        lateinit var testYamlObjectString: String
    }
}