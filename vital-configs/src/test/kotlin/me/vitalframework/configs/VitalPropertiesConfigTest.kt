package me.vitalframework.configs

import org.junit.jupiter.api.*
import org.springframework.util.ResourceUtils

open class VitalPropertiesConfigTest {
    @Test
    fun `config content should not be initialized`() {
        assertThrows<UninitializedPropertyAccessException> {
            val testPropertiesConfig = VitalTestPropertiesConfig()
            testPropertiesConfig.testPropertiesString
        }
    }

    @Test
    fun `config content should be initialized`() {
        assertDoesNotThrow {
            // load config file into object
            val testPropertiesConfig = VitalTestPropertiesConfig().apply {
                load(ResourceUtils.getFile("classpath:test.properties").inputStream())
            }
            // test that the file has been loaded successfully
            assert(testPropertiesConfig.testPropertiesString == "testPropertiesString")
        }
    }
}