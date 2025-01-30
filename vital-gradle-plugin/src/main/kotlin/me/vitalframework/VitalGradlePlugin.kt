package me.vitalframework

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class VitalGradlePlugin : Plugin<Project> {
    private lateinit var target: Project

    override fun apply(target: Project) {
        this.target = target

        target.afterEvaluate {
            requirePlugin("com.github.johnrengelman.shadow", "8.1.1")

            // not every module requires the core module
            if (hasDependency("me.vitalframework:vital-core")) {
                try {
                    // attempt to register annotation processor for kotlin
                    target.dependencies.add("kapt", "me.vitalframework:vital-core-processor:1.0")
                } catch (ignored: Exception) {
                    // kapt not found, register default
                    target.dependencies.add("annotationProcessor", "me.vitalframework:vital-core-processor:1.0")
                }
            }

            // if vital-commands is used, also add processor for module
            if (hasDependency("me.vitalframework:vital-commands")) {
                try {
                    // attempt to register annotation processor for kotlin
                    target.dependencies.add("kapt", "me.vitalframework:vital-commands-processor:1.0")
                } catch (ignored: Exception) {
                    // kapt not found, register default
                    target.dependencies.add("annotationProcessor", "me.vitalframework:vital-commands-processor:1.0")
                }
            }

            target.tasks.named("build") {
                it.dependsOn(target.tasks.named("shadowJar"))
            }
        }
    }

    private fun requirePlugin(pluginNotation: String, exampleVersion: String) {
        try {
            // this will fail when plugins are not detected on classpath
            target.plugins.apply(pluginNotation)
        } catch (e: Exception) {
            throw GradleException("could not find plugin '$pluginNotation' please declare it in 'plugins {}' block, e.g: 'id(\"$pluginNotation\") version \"$exampleVersion\"'")
        }
    }

    private fun hasDependency(dependencyNotation: String): Boolean =
        target.configurations.flatMap { it.allDependencies }
            .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }
}