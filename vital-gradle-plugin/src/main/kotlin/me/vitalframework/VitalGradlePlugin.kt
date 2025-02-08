package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project

class VitalGradlePlugin : Plugin<Project> {
    private lateinit var target: Project

    override fun apply(target: Project) {
        this.target = target

        target.afterEvaluate {
            requirePlugin(SHADOW_PLUGIN_ID, SHADOW_PLUGIN_EXAMPLE_VERSION)
            target.dependencies.add("implementation", "$VITAL_CORE_DEPENDENCY:1.0")

            try {
                // attempt to register annotation processor for kotlin
                target.dependencies.add("kapt", "$VITAL_CORE_PROCESSOR_DEPENDENCY:1.0")
            } catch (e: Exception) {
                // kapt not found, register default
                target.dependencies.add("annotationProcessor", "$VITAL_CORE_PROCESSOR_DEPENDENCY:1.0")
            }
            // if vital-commands is used, also add processor for module
            if (hasDependency(VITAL_COMMANDS_DEPENDENCY)) {
                try {
                    // attempt to register annotation processor for kotlin
                    target.dependencies.add("kapt", "$VITAL_COMMANDS_PROCESSOR_DEPENDENCY:1.0")
                } catch (e: Exception) {
                    // kapt not found, register default
                    target.dependencies.add("annotationProcessor", "$VITAL_COMMANDS_PROCESSOR_DEPENDENCY:1.0")
                }
            }

            target.tasks.named("build") {
                it.dependsOn(target.tasks.named("shadowJar"))
            }
        }
    }

    private fun requirePlugin(id: String, exampleVersion: String) {
        try {
            // this will fail when plugins are not detected on classpath
            target.plugins.apply(id)
        } catch (e: Exception) {
            throw VitalGradlePluginException.PluginNotFound(id, exampleVersion)
        }
    }

    private fun hasDependency(dependencyNotation: String) =
        target.configurations.flatMap { it.allDependencies }
            .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }

    companion object {
        const val SHADOW_PLUGIN_ID = "com.github.johnrengelman.shadow"
        const val SHADOW_PLUGIN_EXAMPLE_VERSION = "8.1.1"
        const val VITAL_COMMANDS_DEPENDENCY = "me.vitalframework:vital-commands"
        const val VITAL_COMMANDS_PROCESSOR_DEPENDENCY = "me.vitalframework:vital-commands-processor"
        const val VITAL_CORE_DEPENDENCY = "me.vitalframework:vital-core"
        const val VITAL_CORE_PROCESSOR_DEPENDENCY = "me.vitalframework:vital-core-processor"
    }
}