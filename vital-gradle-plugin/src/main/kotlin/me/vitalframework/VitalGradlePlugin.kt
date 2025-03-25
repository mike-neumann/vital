package me.vitalframework

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // needs to be applied before dependency resolution takes place.
        target.applyPlugin(Plugin.SPRING_BOOT_ID)
        target.applyPlugin(Plugin.KOTLIN_SPRING_ID)
        target.applyPlugin(Plugin.DEPENDENCY_MANAGEMENT_ID)
        target.applyPlugin(Plugin.SHADOW_ID)

        target.applyDependency("implementation", "${Dependency.VITAL_CORE}:+")
        target.applyDependency(
            "kapt", "${Dependency.VITAL_CORE_PROCESSOR}:+",
            "annotationProcessor", "${Dependency.VITAL_CORE_PROCESSOR}:+"
        )

        if (target.hasDependency(Dependency.VITAL_COMMANDS)) {
            target.applyDependency(
                "kapt", "${Dependency.VITAL_COMMANDS_PROCESSOR}:+",
                "annotationProcessor", "${Dependency.VITAL_COMMANDS_PROCESSOR}:+"
            )
        }

        target.tasks.named("build") { it.dependsOn(target.tasks.named("shadowJar")) }
        // spring's bootJar task should not be enabled, since our server manages its own runtime main class.
        target.tasks.named("bootJar") { it.enabled = false }
        target.tasks.named("shadowJar", ShadowJar::class.java) {
            it.mergeServiceFiles()
            it.mergeGroovyExtensionModules()
//            TODO: currently does not work with logger factories on paper...
//            appendTransform(it, "META-INF/spring.factories")
//            appendTransform(it, "META-INF/spring.handlers")
//            appendTransform(it, "META-INF/spring.schemas")
//            appendTransform(it, "META-INF/spring.tooling")
//            appendTransform(it, "META-INF/web-fragment.xml")
//            appendTransform(it, "META-INF/web-fragment.xml")
            it.appendTransform("META-INF/spring/aot.factories")
            it.appendTransform("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
        }
    }

    private fun ShadowJar.appendTransform(resource: String) = transform(AppendingTransformer::class.java) { it.resource.set(resource) }
    private fun Project.applyPlugin(id: String, exampleVersion: String = "+") = try {
        // this will fail when plugins are not detected on classpath
        plugins.apply(id)
    } catch (_: Exception) {
        throw VitalGradlePluginException.PluginNotFound(id, exampleVersion)
    }

    private fun Project.hasDependency(dependencyNotation: String) = configurations.flatMap { it.allDependencies }
        .any { "${it.group}:${it.name}:${it.version}".startsWith(dependencyNotation) }

    private fun Project.applyDependency(
        configurationName: String,
        dependencyNotation: String,
        fallbackConfigurationName: String = configurationName,
        fallbackDependencyNotation: String = dependencyNotation,
    ) = try {
        dependencies.add(configurationName, dependencyNotation)
    } catch (_: Exception) {
        dependencies.add(fallbackConfigurationName, fallbackDependencyNotation)
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