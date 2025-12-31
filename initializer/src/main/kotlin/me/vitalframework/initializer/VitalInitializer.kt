package me.vitalframework.initializer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.measureTimeMillis

fun main() {
    println()
    println("""
 __     __  _   _             _     ___           _   _     _           _   _                     
 \ \   / / (_) | |_    __ _  | |   |_ _|  _ __   (_) | |_  (_)   __ _  | | (_)  ____   ___   _ __ 
  \ \ / /  | | | __|  / _` | | |    | |  | '_ \  | | | __| | |  / _` | | | | | |_  /  / _ \ | '__|
   \ V /   | | | |_  | (_| | | |    | |  | | | | | | | |_  | | | (_| | | | | |  / /  |  __/ | |   
    \_/    |_|  \__|  \__,_| |_|   |___| |_| |_| |_|  \__| |_|  \__,_| |_| |_| /___|  \___| |_|   
    """.trimIndent())

    println()
    println("Welcome to the Vital-Initializer!")
    println("This script will automatically generate a working plugin after setup is complete.")
    println("You can press 'CONTROL+C' at any time to cancel.")
    println()

    println("What should be the name of your plugin?")
    print(" > ")
    val name = readln().trim().replace(" ", "-")
    println()

    println("What should be the description of your plugin?")
    print(" > ")
    val description = readln().trim()
    println()

    println("What should be the version of your plugin?")
    print(" > ")
    val version = readln().trim()
    println()

    println("What should be the api-version of your plugin?")
    print(" > ")
    val apiVersion = readln().trim()
    println()

    println("What should be the authors of your plugin? (separated by spaces).")
    print(" > ")
    val authors = readln().trim().split(" ")
    println()

    println("What kind of plugin do you wish to create? [${PluginEnvironment.entries.joinToString { it.name }}].")
    var pluginEnvironment: PluginEnvironment? = null
    do {
        print(" > ")
        val answer = readln()
        try {
            pluginEnvironment = PluginEnvironment.valueOf(answer.uppercase())
        } catch (_: IllegalArgumentException) {
            println("Invalid plugin environment, please provide a valid answer.")
        }
    } while (pluginEnvironment == null)
    println()

    println("What should be the programming language for your plugin? [${ProgrammingLanguage.entries.joinToString { it.name }}].")
    var programmingLanguage: ProgrammingLanguage? = null
    do {
        print(" > ")
        val answer = readln()
        try {
            programmingLanguage = ProgrammingLanguage.valueOf(answer.uppercase())
        } catch (_: IllegalArgumentException) {
            println("Invalid programming language, please provide a valid answer.")
        }
    } while (programmingLanguage == null)
    println()

    println("What should be the programming language of the Gradle DSL? [${GradleDslProgrammingLanguage.entries.joinToString { it.name }}].")
    var gradleDslProgrammingLanguage: GradleDslProgrammingLanguage? = null
    do {
        print(" > ")
        val answer = readln()
        try {
            gradleDslProgrammingLanguage = GradleDslProgrammingLanguage.valueOf(answer.uppercase())
        } catch (_: IllegalArgumentException) {
            println("Invalid programming language, please provide a valid answer.")
        }
    } while (gradleDslProgrammingLanguage == null)
    println()

    println()
    println("Vital-Initializer will use the following configuration to generate your plugin:")
    println("Name: '$name'.")
    println("Description: '$description'.")
    println("Authors: '$authors'.")
    println("Version: '$version'.")
    println("API Version: '$apiVersion'.")
    println("Plugin Environment: '$pluginEnvironment'.")
    println("Programming Language: '$programmingLanguage'.")
    println("Gradle DSL Programming Language: '$gradleDslProgrammingLanguage'.")
    println()
    println("Press enter to continue.")
    readln()
    println()

    generatePlugin(name, description, authors, version, apiVersion, pluginEnvironment, programmingLanguage, gradleDslProgrammingLanguage)
}

fun generatePlugin(
    name: String,
    description: String,
    authors: List<String>,
    version: String,
    apiVersion: String,
    pluginEnvironment: PluginEnvironment,
    programmingLanguage: ProgrammingLanguage,
    gradleDslProgrammingLanguage: GradleDslProgrammingLanguage
) {
    println("Generating plugin, please wait.")

    val template = Path.of("template").absolute()
    val parent = Path.of("generated").createDirectories().absolute()
    val target = parent.resolve(name).absolute()

    val targetFile = target.toFile()
    if (targetFile.exists()) {
        println("Target file '${targetFile}' already exists, deleting it.")
        target.toFile().deleteRecursively()
    }

    for (file in Files.walk(template)) {
        val templateFile = template.relativize(file)
        val targetFile = target.resolve(templateFile).createDirectories()
        Files.copy(template.resolve(templateFile), targetFile, StandardCopyOption.REPLACE_EXISTING)
    }

    val time = measureTimeMillis {
        setupGradleDsl(target, gradleDslProgrammingLanguage, programmingLanguage, version, pluginEnvironment, name)
        setupProgrammingLanguage(target, programmingLanguage, name, description, version, apiVersion, authors, pluginEnvironment)
    }

    println("Plugin successfully generated to '$target' in ${time}ms.")
    println("Copy this generated plugin into your preferred project-location and open it in your IDE.")
    println()
    println("NOTE: Although this generated plugin will work with basically all IDEs, it only contains run-configurations for IntelliJ.")
    println("NOTE: If you're not using IntelliJ, you have to manually create run-configurations for your IDE, sorry.")
}

fun setupGradleDsl(target: Path, gradleDslProgrammingLanguage: GradleDslProgrammingLanguage, programmingLanguage: ProgrammingLanguage, version: String, pluginEnvironment: PluginEnvironment, name: String) {
    when (gradleDslProgrammingLanguage) {
        GradleDslProgrammingLanguage.KOTLIN -> {
            Files.delete(target.resolve("build.gradle"))
            Files.delete(target.resolve("settings.gradle"))

            val buildGradle = target.resolve("build.gradle.kts")
            val settingsGradle = target.resolve("settings.gradle.kts")

            val finalBuildGradle = buildGradle.readText()
                .replace("{plugins}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> "java"
                    ProgrammingLanguage.GROOVY -> "groovy"
                    ProgrammingLanguage.KOTLIN -> """
                        kotlin("jvm") version "2.2.0"
                            kotlin("kapt") version "2.2.0"
                    """.trimIndent()
                })
                .replace("{version}", version)
                .replace("{pluginEnvironment}", when (pluginEnvironment) {
                    PluginEnvironment.SPIGOT -> """compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.PAPER -> """compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.BUNGEE -> """compileOnly("net.md-5:bungeecord-api:1.21-R0.3")"""
                })
                .replace("{additionalDependencies}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> ""
                    ProgrammingLanguage.KOTLIN -> ""
                    ProgrammingLanguage.GROOVY -> """implementation("org.apache.groovy:groovy:5.0.0")"""
                })
                .replace("{additionalConfigurations}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> ""
                    ProgrammingLanguage.KOTLIN -> """
                        kotlin {
                            jvmToolchain(24)
                        }
                    """.trimIndent()
                    ProgrammingLanguage.GROOVY -> """
                        tasks.compileGroovy {
                            groovyOptions.javaAnnotationProcessing = true
                        }
                    """.trimIndent()
                })
            val finalSettingsGradle = settingsGradle.readText()
                .replace("{name}", name)

            buildGradle.writeText(finalBuildGradle)
            settingsGradle.writeText(finalSettingsGradle)
        }
        GradleDslProgrammingLanguage.GROOVY -> {
            Files.delete(target.resolve("build.gradle.kts"))
            Files.delete(target.resolve("settings.gradle.kts"))

            val buildGradle = target.resolve("build.gradle")
            val settingsGradle = target.resolve("settings.gradle")

            val finalBuildGradle = buildGradle.readText()
                .replace("{plugins}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> "java"
                    ProgrammingLanguage.GROOVY -> "groovy"
                    ProgrammingLanguage.KOTLIN -> """
                        kotlin("jvm") version "2.2.0"
                            kotlin("kapt) version "2.2.0"
                    """.trimIndent()
                })
                .replace("{version}", version)
                .replace("{pluginEnvironment}", when (pluginEnvironment) {
                    PluginEnvironment.SPIGOT -> """compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.PAPER -> """compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.BUNGEE -> """compileOnly("net.md-5:bungeecord-api:1.21-R0.3")"""
                })
                .replace("{additionalDependencies}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> ""
                    ProgrammingLanguage.KOTLIN -> ""
                    ProgrammingLanguage.GROOVY -> """implementation("org.apache.groovy:groovy:5.0.0")"""
                })
                .replace("{additionalConfigurations}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> ""
                    ProgrammingLanguage.KOTLIN -> """
                        kotlin {
                            jvmToolchain(24)
                        }
                    """.trimIndent()
                    ProgrammingLanguage.GROOVY -> """
                        tasks.compileGroovy {
                            groovyOptions.javaAnnotationProcessing = true
                        }
                    """.trimIndent()
                })

            val finalSettingsGradle = settingsGradle.readText()
                .replace("{name}", name)

            buildGradle.writeText(finalBuildGradle)
            settingsGradle.writeText(finalSettingsGradle)
        }
    }
}

fun setupProgrammingLanguage(target: Path, programmingLanguage: ProgrammingLanguage, name: String, description: String, version: String, apiVersion: String, authors: List<String>, pluginEnvironment: PluginEnvironment) {
    when (programmingLanguage) {
        ProgrammingLanguage.JAVA -> {
            target.resolve("src/main/groovy").toFile().deleteRecursively()
            target.resolve("src/main/kotlin").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/java/me/myproject/MyJavaPlugin.java")
            val finalPluginMainClass = pluginMainClass.readText()
                .replace("{name}", name)
                .replace("{description}", description)
                .replace("{version}", version)
                .replace("{apiVersion}", apiVersion)
                .replace("{author}", authors.joinToString(", ") { "\"$it\"" })
                .replace("{pluginEnvironment}", "Vital.PluginEnvironment.${pluginEnvironment}")

            pluginMainClass.writeText(finalPluginMainClass)
        }
        ProgrammingLanguage.KOTLIN -> {
            target.resolve("src/main/groovy").toFile().deleteRecursively()
            target.resolve("src/main/java").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/kotlin/me/myproject/MyKotlinPlugin.kt")
            val finalPluginMainClass = pluginMainClass.readText()
                .replace("{name}", name)
                .replace("{description}", description)
                .replace("{version}", version)
                .replace("{apiVersion}", apiVersion)
                .replace("{author}", authors.joinToString(", ") { "\"$it\"" })
                .replace("{pluginEnvironment}", "Vital.PluginEnvironment.${pluginEnvironment}")

            pluginMainClass.writeText(finalPluginMainClass)
        }
        ProgrammingLanguage.GROOVY -> {
            target.resolve("src/main/java").toFile().deleteRecursively()
            target.resolve("src/main/kotlin").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/groovy/me/myproject/MyGroovyPlugin.groovy")
            val finalPluginMainClass = pluginMainClass.readText()
                .replace("{name}", name)
                .replace("{description}", description)
                .replace("{version}", version)
                .replace("{apiVersion}", apiVersion)
                .replace("{author}", authors.joinToString(", ") { "\"$it\"" })
                .replace("{pluginEnvironment}", "Vital.PluginEnvironment.${pluginEnvironment}")

            pluginMainClass.writeText(finalPluginMainClass)
        }
    }
}

enum class PluginEnvironment {
    SPIGOT, PAPER, BUNGEE
}

enum class ProgrammingLanguage {
    JAVA, KOTLIN, GROOVY
}

enum class GradleDslProgrammingLanguage {
    KOTLIN, GROOVY
}
