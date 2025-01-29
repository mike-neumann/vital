package me.vitalframework.configs

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.util.ResourceUtils
import java.io.File

open class VitalPropertiesConfigTest {
    @AfterEach
    fun afterEach() {
        // after each test, delete test.properties
        File("test.properties").delete()
    }

    @Test
    fun `config content should not be initialized`() {
        assertThrows<UninitializedPropertyAccessException> {
            val testPropertiesConfig = VitalTestPropertiesConfig()

            testPropertiesConfig.testPropertiesString
        }
    }

    @Test
    fun `config content should be saved`() {
        assertDoesNotThrow {
            val testPropertiesConfig = VitalTestPropertiesConfig().apply {
                testPropertiesString = "testPropertiesString"
            }

            testPropertiesConfig.save()
            assert(File("test.properties").exists())
        }
    }

    @Test
    fun `config content should be initialized`() {
        assertDoesNotThrow {
            // copy predefined test file to config location
            ResourceUtils.getFile("classpath:predefinedTest.properties").copyTo(File("test.properties"))

            // load config file into object
            val testPropertiesConfig = VitalTestPropertiesConfig()

            // test that the file has been loaded successfully
            assert(testPropertiesConfig.testPropertiesString == "testPropertiesString")
        }
    }
}