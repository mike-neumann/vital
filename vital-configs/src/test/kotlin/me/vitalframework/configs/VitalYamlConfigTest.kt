package me.vitalframework.configs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.util.ResourceUtils

open class VitalYamlConfigTest {
    @Test
    fun `config content should not be initialized`() {
        val testYamlConfig = VitalTestYamlConfig()

        assertThrows<UninitializedPropertyAccessException> {
            testYamlConfig.testYamlString
            testYamlConfig.testYamlObject
            testYamlConfig.testYamlObject.testYamlObjectString
        }
    }

    @Test
    fun `config content should be initialized`() {
        // load config file into object
        val testYamlConfig =
            VitalTestYamlConfig().apply {
                load(ResourceUtils.getFile("classpath:test.yaml").inputStream())
            }
        // test that the file has been loaded successfully
        assert(testYamlConfig.testYamlString == "testYamlString")
        assert(testYamlConfig.testYamlObject.testYamlObjectString == "testYamlObjectString")
    }
}
