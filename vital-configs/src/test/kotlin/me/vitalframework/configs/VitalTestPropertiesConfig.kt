package me.vitalframework.configs

import me.vitalframework.configs.processor.VitalPropertiesConfigFileProcessor

@VitalConfig.Info("test.properties", processor = VitalPropertiesConfigFileProcessor::class)
class VitalTestPropertiesConfig : VitalConfig() {
    @Property(String::class)
    lateinit var testPropertiesString: String
}