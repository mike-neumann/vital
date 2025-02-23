package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginAware

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // needs to be applied before dependency resolution takes place.
        applyPlugin(target, PLUGIN_SPRING_BOOT_ID, PLUGIN_SPRING_BOOT_EXAMPLE_VERSION)
        applyPlugin(target, PLUGIN_KOTLIN_SPRING_ID, PLUGIN_KOTLIN_SPRING_EXAMPLE_VERSION)
        applyPlugin(target, PLUGIN_DEPENDENCY_MANAGEMENT_ID, PLUGIN_DEPENDENCY_MANAGEMENT_EXAMPLE_VERSION)
        applyPlugin(target, PLUGIN_SHADOW_ID, PLUGIN_SHADOW_PLUGIN_EXAMPLE_VERSION)

        applyDependency(target, "implementation", "$DEPENDENCY_VITAL_CORE:1.0")
        applyDependency(
            target, "kapt", "$DEPENDENCY_VITAL_CORE_PROCESSOR:1.0",
            "annotationProcessor", "$DEPENDENCY_VITAL_CORE_PROCESSOR:1.0"
        )

        if (hasDependency(target, DEPENDENCY_VITAL_COMMANDS)) {
            applyDependency(
                target, "kapt", "$DEPENDENCY_VITAL_COMMANDS_PROCESSOR:1.0",
                "annotationProcessor", "$DEPENDENCY_VITAL_COMMANDS_PROCESSOR:1.0"
            )
        }

        target.tasks.named("build") { it.dependsOn(target.tasks.named("shadowJar")) }
        // spring's bootJar task should not be enabled, since our server manages its own runtime main class.
        target.tasks.named("bootJar") { it.enabled = false }
    }

    private fun applyPlugin(project: Project, id: String, exampleVersion: String) {
        try {
            // this will fail when plugins are not detected on classpath
            (project as PluginAware).plugins.apply(id)
        } catch (e: Exception) {
            throw VitalGradlePluginException.PluginNotFound(id, exampleVersion)
        }
    }

    private fun hasDependency(project: Project, dependencyNotation: String) =
        project.configurations.flatMap { it.allDependencies }
            .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }

    private fun applyDependency(
        project: Project,
        configurationName: String,
        dependencyNotation: String,
        fallbackConfigurationName: String = configurationName,
        fallbackDependencyNotation: String = dependencyNotation,
    ) {
        try {
            project.dependencies.add(configurationName, dependencyNotation)
        } catch (e: Exception) {
            project.dependencies.add(fallbackConfigurationName, fallbackDependencyNotation)
        }
    }

    companion object {
        const val PLUGIN_SPRING_BOOT_ID = "org.springframework.boot"
        const val PLUGIN_SPRING_BOOT_EXAMPLE_VERSION = "3.2.2"
        const val PLUGIN_KOTLIN_SPRING_ID = "org.jetbrains.kotlin.plugin.spring"
        const val PLUGIN_KOTLIN_SPRING_EXAMPLE_VERSION = "2.1.0"
        const val PLUGIN_DEPENDENCY_MANAGEMENT_ID = "io.spring.dependency-management"
        const val PLUGIN_DEPENDENCY_MANAGEMENT_EXAMPLE_VERSION = "1.1.7"
        const val PLUGIN_SHADOW_ID = "com.gradleup.shadow"
        const val PLUGIN_SHADOW_PLUGIN_EXAMPLE_VERSION = "8.3.6"
        const val DEPENDENCY_VITAL_COMMANDS = "me.vitalframework:vital-commands"
        const val DEPENDENCY_VITAL_COMMANDS_VERSION = "1.0"
        const val DEPENDENCY_VITAL_COMMANDS_PROCESSOR = "me.vitalframework:vital-commands-processor"
        const val DEPENDENCY_VITAL_COMMANDS_PROCESSOR_VERSION = "1.0"
        const val DEPENDENCY_VITAL_CORE = "me.vitalframework:vital-core"
        const val DEPENDENCY_VITAL_CORE_VERSION = "1.0"
        const val DEPENDENCY_VITAL_CORE_PROCESSOR = "me.vitalframework:vital-core-processor"
        const val DEPENDENCY_VITAL_CORE_PROCESSOR_VERSION = "1.0"
    }
}