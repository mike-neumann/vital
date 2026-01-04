package me.vitalframework.initializer.test

import me.vitalframework.initializer.DataModel
import me.vitalframework.initializer.GradleDslProgrammingLanguage
import me.vitalframework.initializer.PluginEnvironment
import me.vitalframework.initializer.ProgrammingLanguage
import me.vitalframework.initializer.createFreemarkerConfiguration
import me.vitalframework.initializer.generatePlugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VitalInitializerTest {
    fun methodSource() = listOf(
        Arguments.of("spigot-java-kotlin", "spigot-java-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.SPIGOT, ProgrammingLanguage.JAVA, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("spigot-java-groovy", "spigot-java-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.SPIGOT, ProgrammingLanguage.JAVA, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("spigot-kotlin-kotlin", "spigot-kotlin-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.SPIGOT, ProgrammingLanguage.KOTLIN, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("spigot-kotlin-groovy", "spigot-kotlin-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.SPIGOT, ProgrammingLanguage.KOTLIN, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("spigot-groovy-kotlin", "spigot-groovy-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.SPIGOT, ProgrammingLanguage.GROOVY, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("spigot-groovy-groovy", "spigot-groovy-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.SPIGOT, ProgrammingLanguage.GROOVY, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("paper-java-groovy", "paper-java-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.PAPER, ProgrammingLanguage.JAVA, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("paper-java-kotlin", "paper-java-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.PAPER, ProgrammingLanguage.JAVA, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("paper-kotlin-kotlin", "paper-kotlin-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.PAPER, ProgrammingLanguage.KOTLIN, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("paper-kotlin-groovy", "paper-kotlin-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.PAPER, ProgrammingLanguage.KOTLIN, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("paper-groovy-kotlin", "paper-groovy-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.PAPER, ProgrammingLanguage.GROOVY, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("paper-groovy-groovy", "paper-groovy-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.PAPER, ProgrammingLanguage.GROOVY, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("bungee-java-kotlin", "bungee-java-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.BUNGEE, ProgrammingLanguage.JAVA, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("bungee-java-groovy", "bungee-java-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.BUNGEE, ProgrammingLanguage.JAVA, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("bungee-kotlin-kotlin", "bungee-kotlin-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.BUNGEE, ProgrammingLanguage.KOTLIN, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("bungee-kotlin-groovy", "bungee-kotlin-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.BUNGEE, ProgrammingLanguage.KOTLIN, GradleDslProgrammingLanguage.GROOVY),
        Arguments.of("bungee-groovy-kotlin", "bungee-groovy-kotlin", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.BUNGEE, ProgrammingLanguage.GROOVY, GradleDslProgrammingLanguage.KOTLIN),
        Arguments.of("bungee-groovy-groovy", "bungee-groovy-groovy", "1.0.0", "1.21", listOf(1, 2, 3), PluginEnvironment.BUNGEE, ProgrammingLanguage.GROOVY, GradleDslProgrammingLanguage.GROOVY)
    )

    @MethodSource("methodSource")
    @ParameterizedTest
    fun `plugin generation should work`(name: String, description: String, version: String, apiVersion: String, authors: List<String>, pluginEnvironment: PluginEnvironment, programmingLanguage: ProgrammingLanguage, gradleDslProgrammingLanguage: GradleDslProgrammingLanguage) {
        Assertions.assertDoesNotThrow {
            generatePlugin(
                createFreemarkerConfiguration(),
                DataModel(name, description, version, apiVersion, authors, pluginEnvironment, programmingLanguage, gradleDslProgrammingLanguage)
            )
        }
    }
}