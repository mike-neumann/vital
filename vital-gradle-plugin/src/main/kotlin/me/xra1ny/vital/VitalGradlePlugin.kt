package me.xra1ny.vital

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class VitalGradlePlugin : Plugin<Project> {
    private lateinit var target: Project

    override fun apply(target: Project) {
        this.target = target

        target.afterEvaluate {
            // default dependencies
            //target.dependencies.add("implementation", "org.springframework.boot:spring-boot-starter")
            //target.dependencies.add("implementation", "org.springframework:spring-context:6.1.10")
            //target.dependencies.add("implementation", "me.xra1ny.vital:vital-core:1.0")
            //target.dependencies.add("annotationProcessor", "me.xra1ny.vital:vital-core-processor:1.0")

            // apply plugins needed for vital to work
            //requirePlugin("org.springframework.boot", "3.2.4")
            //requirePlugin("io.spring.dependency-management", "1.1.4")
            //requirePlugin("com.github.johnrengelman.shadow", "8.1.1")

            // if vital-commands is used, also add processor for module
            if (hasDependency("me.xra1ny.vital:vital-commands")) {
                target.dependencies.add("annotationProcessor", "me.xra1ny.vital:vital-commands-processor:1.0")
            }

            if (hasDependency("me.xra1ny.vital:vital-configs")) {
                target.dependencies.add("implementation", "me.xra1ny.essentia:essentia-configure:1.0")
            }

            //target.tasks.named("bootJar") {
            //    it.enabled = false
            //}

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
            target.logger.log(
                LogLevel.ERROR,
                "could not find plugin '$pluginNotation' please declare it in 'plugins {}' block, e.g: 'id(\"$pluginNotation\") version \"$exampleVersion\"'"
            )
        }
    }

    private fun hasDependency(dependencyNotation: String): Boolean {
        return target.configurations.flatMap { it.allDependencies }
            .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }
    }
}