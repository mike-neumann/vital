package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginAware

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // needs to be applied before dependency resolution takes place.
        applyPlugin(target, Plugin.SPRING_BOOT_ID)
        applyPlugin(target, Plugin.KOTLIN_SPRING_ID)
        applyPlugin(target, Plugin.DEPENDENCY_MANAGEMENT_ID)
        applyPlugin(target, Plugin.SHADOW_ID)

        applyDependency(target, "implementation", "${Dependency.VITAL_CORE}:+")
        applyDependency(
            target, "kapt", "${Dependency.VITAL_CORE_PROCESSOR}:+",
            "annotationProcessor", "${Dependency.VITAL_CORE_PROCESSOR}:+"
        )

        if (hasDependency(target, Dependency.VITAL_COMMANDS)) {
            applyDependency(
                target, "kapt", "${Dependency.VITAL_COMMANDS_PROCESSOR}:+",
                "annotationProcessor", "${Dependency.VITAL_COMMANDS_PROCESSOR}:+"
            )
        }

        target.tasks.named("build") { it.dependsOn(target.tasks.named("shadowJar")) }
        // spring's bootJar task should not be enabled, since our server manages its own runtime main class.
        target.tasks.named("bootJar") { it.enabled = false }
    }

    private fun applyPlugin(project: Project, id: String, exampleVersion: String = "+") {
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

    object Plugin {
        const val SPRING_BOOT_ID = "org.springframework.boot"
        const val KOTLIN_SPRING_ID = "org.jetbrains.kotlin.plugin.spring"
        const val DEPENDENCY_MANAGEMENT_ID = "io.spring.dependency-management"
        const val SHADOW_ID = "com.gradleup.shadow"
    }

    object Dependency {
        const val VITAL_COMMANDS = "me.vitalframework:vital-commands"
        const val VITAL_COMMANDS_PROCESSOR = "me.vitalframework:vital-commands-processor"
        const val VITAL_CORE = "me.vitalframework:vital-core"
        const val VITAL_CORE_PROCESSOR = "me.vitalframework:vital-core-processor"
    }
}