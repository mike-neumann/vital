package me.vitalframework.initializer

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.measureTimeMillis

fun <T> readln(prompt: String, answers: List<String> = emptyList(), transform: (String) -> T): T {
    println("$prompt${if (answers.isEmpty()) "" else " [${answers.joinToString()}]."}")
    var answer: T? = null
    do {
        print(" > ")
        val input = readln()
        try {
            answer = transform(input)
        } catch (_: IllegalArgumentException) {
            println("Invalid answer.")
        }
    } while (answer == null)
    println()

    return answer
}

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
    println("If you're running through the terminal, you can press 'CONTROL+C' at any time to cancel.")
    println("If you're running through IntelliJ, you can press the red Stop-Button at the top-right to cancel at any time.")
    println()

    val name = readln("What should be the name of your plugin?") {
        it.trim().also {
            if (it.contains(" ")) {
                println("WARNING: Plugin names cannot contain spaces. Your plugin name will be converted to '${it.replace(" ", "-")}'.")
                println("Press enter to continue.")
                readln()
            }
        }.replace(" ", "-")
    }
    val description = readln("What should be the description of your plugin?") { it.trim() }
    val version = readln("What should be the version of your plugin?") {
        it.trim().also {
            if (!it.matches("[A-Za-z0-9]+\\.[A-Za-z0-9]+\\.[A-Za-z0-9]+".toRegex())) {
                println("WARNING: Looks like you're not using semantic versioning (SemVer) for your plugin!")
                println("WARNING: In software development, it is recommended to use semantic versioning (SemVer). https://www.geeksforgeeks.org/software-engineering/introduction-semantic-versioning/")
                println("Press enter to continue.")
                readln()
            }
        }
    }
    val apiVersion = readln("What should be the api-version of your plugin? (e.g. 1.21, 1.20, etc.)") {
        it.trim().also {
            if (!it.matches("[0-9]\\.[0-9]+".toRegex())) {
                throw IllegalArgumentException()
            }
        }
    }
    val authors = readln("What should be the authors of your plugin? (separated by spaces).") { it.trim().split(" ") }
    val pluginEnvironment = readln("What kind of plugin do you want to create?", PluginEnvironment.entries.map { it.name }) { PluginEnvironment.valueOf(it.uppercase()) }
    val programmingLanguage = readln("What should be the programming language for your plugin?", ProgrammingLanguage.entries.map { it.name }) { ProgrammingLanguage.valueOf(it.uppercase()) }
    val gradleDslProgrammingLanguage = readln("What should be the programming language of the Gradle DSL?", GradleDslProgrammingLanguage.entries.map { it.name }) { GradleDslProgrammingLanguage.valueOf(it.uppercase()) }

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

    val time = measureTimeMillis {
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

        setupGradleDsl(target, gradleDslProgrammingLanguage, programmingLanguage, version, pluginEnvironment, name)
        setupProgrammingLanguage(target, programmingLanguage, name, description, version, apiVersion, authors, pluginEnvironment)
    }

    println("Plugin successfully generated to '$target' in ${time}ms.")
    println("Move this generated plugin into your preferred project-location and open it in your IDE.")
    println()
    println("NOTE: Although this generated plugin should work with all IDEs, it only contains pre-configured run-configurations for IntelliJ.")
}

fun setupGradleDsl(target: Path, gradleDslProgrammingLanguage: GradleDslProgrammingLanguage, programmingLanguage: ProgrammingLanguage, version: String, pluginEnvironment: PluginEnvironment, name: String) {
    when (gradleDslProgrammingLanguage) {
        GradleDslProgrammingLanguage.KOTLIN -> {
            Files.delete(target.resolve("build.gradle"))
            Files.delete(target.resolve("settings.gradle"))

            val buildGradle = target.resolve("build.gradle.kts")
            val settingsGradle = target.resolve("settings.gradle.kts")

            val finalBuildGradle = buildGradle.readText()
                .replace($$"${plugins}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> "java"
                    ProgrammingLanguage.GROOVY -> "groovy"
                    ProgrammingLanguage.KOTLIN -> """
                        kotlin("jvm") version "2.2.0"
                            kotlin("kapt") version "2.2.0"
                    """.trimIndent()
                })
                .replace($$"${version}", version)
                .replace($$"${pluginEnvironment}", when (pluginEnvironment) {
                    PluginEnvironment.SPIGOT -> """compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.PAPER -> """compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.BUNGEE -> """compileOnly("net.md-5:bungeecord-api:1.21-R0.3")"""
                })
                .replace($$"${additionalDependencies}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> ""
                    ProgrammingLanguage.KOTLIN -> ""
                    ProgrammingLanguage.GROOVY -> """implementation("org.apache.groovy:groovy:5.0.0")"""
                })
                .replace($$"${additionalConfigurations}", when (programmingLanguage) {
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
                .replace($$"${name}", name)

            buildGradle.writeText(finalBuildGradle)
            settingsGradle.writeText(finalSettingsGradle)
        }
        GradleDslProgrammingLanguage.GROOVY -> {
            Files.delete(target.resolve("build.gradle.kts"))
            Files.delete(target.resolve("settings.gradle.kts"))

            val buildGradle = target.resolve("build.gradle")
            val settingsGradle = target.resolve("settings.gradle")

            val finalBuildGradle = buildGradle.readText()
                .replace($$"${plugins}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> "java"
                    ProgrammingLanguage.GROOVY -> "groovy"
                    ProgrammingLanguage.KOTLIN -> """
                        kotlin("jvm") version "2.2.0"
                            kotlin("kapt) version "2.2.0"
                    """.trimIndent()
                })
                .replace($$"${version}", version)
                .replace($$"${pluginEnvironment}", when (pluginEnvironment) {
                    PluginEnvironment.SPIGOT -> """compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.PAPER -> """compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")"""
                    PluginEnvironment.BUNGEE -> """compileOnly("net.md-5:bungeecord-api:1.21-R0.3")"""
                })
                .replace($$"${additionalDependencies}", when (programmingLanguage) {
                    ProgrammingLanguage.JAVA -> ""
                    ProgrammingLanguage.KOTLIN -> ""
                    ProgrammingLanguage.GROOVY -> """implementation("org.apache.groovy:groovy:5.0.0")"""
                })
                .replace($$"${additionalConfigurations}", when (programmingLanguage) {
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
                .replace($$"${name}", name)

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
                .replace($$"${name}", name)
                .replace($$"${description}", description)
                .replace($$"${version}", version)
                .replace($$"${apiVersion}", apiVersion)
                .replace($$"${author}", authors.joinToString(", ") { "\"$it\"" })
                .replace($$"${pluginEnvironment}", "Vital.PluginEnvironment.${pluginEnvironment}")

            pluginMainClass.writeText(finalPluginMainClass)
        }
        ProgrammingLanguage.KOTLIN -> {
            target.resolve("src/main/groovy").toFile().deleteRecursively()
            target.resolve("src/main/java").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/kotlin/me/myproject/MyKotlinPlugin.kt")
            val finalPluginMainClass = pluginMainClass.readText()
                .replace($$"${name}", name)
                .replace($$"${description}", description)
                .replace($$"${version}", version)
                .replace($$"${apiVersion}", apiVersion)
                .replace($$"${author}", authors.joinToString(", ") { "\"$it\"" })
                .replace($$"${pluginEnvironment}", "Vital.PluginEnvironment.${pluginEnvironment}")

            pluginMainClass.writeText(finalPluginMainClass)
        }
        ProgrammingLanguage.GROOVY -> {
            target.resolve("src/main/java").toFile().deleteRecursively()
            target.resolve("src/main/kotlin").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/groovy/me/myproject/MyGroovyPlugin.groovy")
            val finalPluginMainClass = pluginMainClass.readText()
                .replace($$"${name}", name)
                .replace($$"${description}", description)
                .replace($$"${version}", version)
                .replace($$"${apiVersion}", apiVersion)
                .replace($$"${author}", authors.joinToString(", ") { "\"$it\"" })
                .replace($$"${pluginEnvironment}", "Vital.PluginEnvironment.${pluginEnvironment}")

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
