package me.xra1ny.vital

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class VitalGradlePlugin : Plugin<Project> {
    private lateinit var target: Project

    override fun apply(target: Project) {
        this.target = target

        target.afterEvaluate {
            // apply plugins needed for vital to work
            requirePlugin("com.github.johnrengelman.shadow", "8.1.1")

            // default dependencies
            target.dependencies.add("implementation", "org.springframework.boot:spring-boot-starter:3.3.1")
            target.dependencies.add("implementation", "me.xra1ny.vital:vital-core:1.0")

            target.dependencies.add("annotationProcessor", "me.xra1ny.vital:vital-core-processor:1.0")
            try {
                // for kotlin support
                target.dependencies.add("kapt", "me.xra1ny.vital:vital-core-processor:1.0")
            } catch (ignored: Exception) {
            }

            // if vital-commands is used, also add processor for module
            if (hasDependency("me.xra1ny.vital:vital-commands")) {
                target.dependencies.add("annotationProcessor", "me.xra1ny.vital:vital-commands-processor:1.0")
                try {
                    // for kotlin support
                    target.dependencies.add("kapt", "me.xra1ny.vital:vital-commands-processor:1.0")
                } catch (ignored: Exception) {
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

    private fun hasDependency(dependencyNotation: String): Boolean {
        return target.configurations.flatMap { it.allDependencies }
            .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }
    }
}