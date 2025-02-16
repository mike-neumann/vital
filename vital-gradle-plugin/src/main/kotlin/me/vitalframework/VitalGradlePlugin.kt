package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.afterEvaluate {
            requirePlugin(it, SHADOW_PLUGIN_ID, SHADOW_PLUGIN_EXAMPLE_VERSION)
            target.dependencies.add("implementation", "$DEPENDENCY_VITAL_CORE:1.0")

            try {
                // attempt to register annotation processor for kotlin
                target.dependencies.add("kapt", "$DEPENDENCY_VITAL_CORE_PROCESSOR:1.0")
            } catch (e: Exception) {
                // kapt not found, register default
                target.dependencies.add("annotationProcessor", "$DEPENDENCY_VITAL_CORE_PROCESSOR:1.0")
            }

            if (hasDependency(it, DEPENDENCY_VITAL_COMMANDS)) {
                // when vital-commands is used, we also want to use vital-commands-processor
                try {
                    // attempt to register annotation processor for kotlin
                    target.dependencies.add("kapt", "$DEPENDENCY_VITAL_COMMANDS_PROCESSOR:1.0")
                } catch (e: Exception) {
                    // kapt not found, register default
                    target.dependencies.add("annotationProcessor", "$DEPENDENCY_VITAL_COMMANDS_PROCESSOR:1.0")
                }
            }

            target.tasks.named("build") { it.dependsOn(target.tasks.named("shadowJar")) }
        }
    }

    private fun requirePlugin(project: Project, id: String, exampleVersion: String) {
        try {
            // this will fail when plugins are not detected on classpath
            project.plugins.apply(id)
        } catch (e: Exception) {
            throw VitalGradlePluginException.PluginNotFound(id, exampleVersion)
        }
    }

    private fun hasDependency(project: Project, dependencyNotation: String) =
        project.configurations.flatMap { it.allDependencies }
            .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }

    companion object {
        const val SHADOW_PLUGIN_ID = "com.gradleup.shadow"
        const val SHADOW_PLUGIN_EXAMPLE_VERSION = "8.3.6"
        const val DEPENDENCY_VITAL_COMMANDS = "me.vitalframework:vital-commands"
        const val DEPENDENCY_VITAL_COMMANDS_PROCESSOR = "me.vitalframework:vital-commands-processor"
        const val DEPENDENCY_VITAL_CORE = "me.vitalframework:vital-core"
        const val DEPENDENCY_VITAL_CORE_PROCESSOR = "me.vitalframework:vital-core-processor"
    }
}