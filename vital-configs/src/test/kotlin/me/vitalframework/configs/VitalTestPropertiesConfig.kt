package me.vitalframework.configs

import me.vitalframework.configs.processor.VitalPropertiesConfigProcessor

@VitalConfig.Info("test.properties", VitalPropertiesConfigProcessor::class)
class VitalTestPropertiesConfig : VitalConfig() {
    @Property(String::class)
    lateinit var testPropertiesString: String
}