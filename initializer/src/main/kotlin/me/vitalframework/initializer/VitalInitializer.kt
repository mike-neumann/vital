package me.vitalframework.initializer

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.name
import kotlin.io.path.writer
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

fun createFreemarkerConfiguration() = Configuration(Configuration.VERSION_2_3_34).apply {
    setDirectoryForTemplateLoading(File("template"))
    defaultEncoding = "UTF-8"
    templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    logTemplateExceptions = false
    wrapUncheckedExceptions = true
    fallbackOnNullLoopVariable = false
}

fun main() {
    val freemarkerConfiguration = createFreemarkerConfiguration()

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

    val dataModel = DataModel(name, description, version, apiVersion, authors, pluginEnvironment, programmingLanguage, gradleDslProgrammingLanguage)
    println()
    println("Vital-Initializer will use the following configuration to generate your plugin:")
    println("Name: '${dataModel.name}'.")
    println("Description: '${dataModel.description}'.")
    println("Authors: '${dataModel.authors}'.")
    println("Version: '${dataModel.version}'.")
    println("API Version: '${dataModel.apiVersion}'.")
    println("Plugin Environment: '${dataModel.pluginEnvironment}'.")
    println("Programming Language: '${dataModel.programmingLanguage}'.")
    println("Gradle DSL Programming Language: '${dataModel.gradleDslProgrammingLanguage}'.")
    println()
    println("Press enter to continue.")
    readln()

    generatePlugin(freemarkerConfiguration, dataModel)
}

fun generatePlugin(freemarkerConfiguration: Configuration, dataModel: DataModel) {
    println("Generating plugin, please wait.")

    val template = Path.of("template").absolute()
    val parent = Path.of("generated").createDirectories().absolute()
    val target = parent.resolve(dataModel.name).absolute()

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

        setupGradleDsl(target, freemarkerConfiguration, dataModel)
        setupProgrammingLanguage(target, freemarkerConfiguration, dataModel)
    }

    println("Plugin successfully generated to '$target' in ${time}ms.")
    println("Move this generated plugin into your preferred project-location and open it in your IDE.")
    println()
    println("NOTE: Although this generated plugin should work with all IDEs, it only contains pre-configured run-configurations for IntelliJ.")
}

fun setupGradleDsl(target: Path, freemarkerConfiguration: Configuration, dataModel: DataModel) {
    when (dataModel.gradleDslProgrammingLanguage) {
        GradleDslProgrammingLanguage.KOTLIN -> {
            Files.delete(target.resolve("build.gradle"))
            Files.delete(target.resolve("settings.gradle"))

            val buildGradle = target.resolve("build.gradle.kts")
            val buildGradleTemplate = freemarkerConfiguration.getTemplate(buildGradle.name)
            buildGradleTemplate.process(dataModel, buildGradle.writer())

            val settingsGradle = target.resolve("settings.gradle.kts")
            val settingsGradleTemplate = freemarkerConfiguration.getTemplate(settingsGradle.name)
            settingsGradleTemplate.process(dataModel, settingsGradle.writer())
        }
        GradleDslProgrammingLanguage.GROOVY -> {
            Files.delete(target.resolve("build.gradle.kts"))
            Files.delete(target.resolve("settings.gradle.kts"))

            val buildGradle = target.resolve("build.gradle")
            val buildGradleTemplate = freemarkerConfiguration.getTemplate(buildGradle.name)
            buildGradleTemplate.process(dataModel, buildGradle.writer())

            val settingsGradle = target.resolve("settings.gradle")
            val settingsGradleTemplate = freemarkerConfiguration.getTemplate(settingsGradle.name)
            settingsGradleTemplate.process(dataModel, settingsGradle.writer())
        }
    }
}

fun setupProgrammingLanguage(target: Path, freemarkerConfiguration: Configuration, dataModel: DataModel) {
    when (dataModel.programmingLanguage) {
        ProgrammingLanguage.JAVA -> {
            target.resolve("src/main/groovy").toFile().deleteRecursively()
            target.resolve("src/main/kotlin").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/java/me/myproject/MyJavaPlugin.java")
            val pluginMainClassTemplate = freemarkerConfiguration.getTemplate("src/main/java/me/myproject/MyJavaPlugin.java")
            pluginMainClassTemplate.process(dataModel, pluginMainClass.writer())
        }
        ProgrammingLanguage.KOTLIN -> {
            target.resolve("src/main/groovy").toFile().deleteRecursively()
            target.resolve("src/main/java").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/kotlin/me/myproject/MyKotlinPlugin.kt")
            val pluginMainClassTemplate = freemarkerConfiguration.getTemplate("src/main/kotlin/me/myproject/MyKotlinPlugin.kt")
            pluginMainClassTemplate.process(dataModel, pluginMainClass.writer())
        }
        ProgrammingLanguage.GROOVY -> {
            target.resolve("src/main/java").toFile().deleteRecursively()
            target.resolve("src/main/kotlin").toFile().deleteRecursively()

            val pluginMainClass = target.resolve("src/main/groovy/me/myproject/MyGroovyPlugin.groovy")
            val pluginMainClassTemplate = freemarkerConfiguration.getTemplate("src/main/groovy/me/myproject/MyGroovyPlugin.groovy")
            pluginMainClassTemplate.process(dataModel, pluginMainClass.writer())
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
