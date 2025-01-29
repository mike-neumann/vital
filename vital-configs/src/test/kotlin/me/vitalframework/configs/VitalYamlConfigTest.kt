package me.vitalframework.configs

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.util.ResourceUtils
import java.io.File

open class VitalYamlConfigTest {
    @AfterEach
    fun afterEach() {
        // after each test, delete test.yaml
        File("test.yaml").delete()
    }

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
    fun `config content should be saved`() {
        val testYamlConfig = VitalTestYamlConfig().apply {
            testYamlString = "testYamlString"
            testYamlObject = VitalTestYamlConfig.TestYamlObject().apply {
                testYamlObjectString = "testYamlObjectString"
            }
        }

        assertDoesNotThrow {
            testYamlConfig.save()
            assert(File("test.yaml").exists())
        }
    }

    @Test
    fun `config content should be initialized`() {
        assertDoesNotThrow {
            // copy predefined test file to config location
            ResourceUtils.getFile("classpath:predefinedTest.yaml").copyTo(File("test.yaml"))

            // load config file into object
            val testYamlConfig = VitalTestYamlConfig()

            // test that the file has been loaded successfully
            assert(testYamlConfig.testYamlString == "testYamlString")
            assert(testYamlConfig.testYamlObject.testYamlObjectString == "testYamlObjectString")
        }
    }
}