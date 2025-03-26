package me.vitalframework

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.AppendingTransformer
import org.gradle.api.Plugin
import org.gradle.api.Project

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // needs to be applied before dependency resolution takes place.
        target.plugins.apply("org.springframework.boot")
        target.plugins.apply("org.jetbrains.kotlin.plugin.spring")
        target.plugins.apply("io.spring.dependency-management")
        target.plugins.apply("com.gradleup.shadow")

        target.applyDependency("implementation", "me.vitalframework:vital-core:${target.getProperty("vitalVersion")}")
        target.applyDependency(
            "kapt", "me.vitalframework:vital-core-processor:${target.getProperty("vitalVersion")}",
            "annotationProcessor", "me.vitalframework:vital-core-processor:${target.getProperty("vitalVersion")}"
        )

        if (target.hasDependency("me.vitalframework:vital-commands")) {
            target.applyDependency(
                "kapt", "me.vitalframework:vital-commands-processor:${target.getProperty("vitalVersion")}",
                "annotationProcessor", "me.vitalframework:vital-commands-processor:${target.getProperty("vitalVersion")}"
            )
        }

        target.tasks.named("build") { it.dependsOn(target.tasks.named("shadowJar")) }
        target.tasks.named("bootJar") { it.enabled = false }
        target.tasks.named("shadowJar", ShadowJar::class.java) {
            // service files are not automatically merged which can cause
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
            it.exclude(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.md",
                "META-INF/NOTICE.txt",
                "META-INF/additional-spring-configuration-metadata.json",
                "META-INF/license.txt",
                "META-INF/notice.txt",
                "META-INF/sisu/javax.inject.Named",
                "META-INF/spring-configuration-metadata.json",
                "META-INF/spring.factories",
                "META-INF/spring.handlers",
                "META-INF/spring.schemas",
                "META-INF/spring.tooling",
                "META-INF/web-fragment.xml",
                "license.txt",
                "notice.txt"
            )
        }
    }

    private fun ShadowJar.appendTransform(resource: String) = transform(AppendingTransformer::class.java) { it.resource.set(resource) }

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

    private fun Project.getProperty(propertyName: String) = findProperty(propertyName)
        ?: throw IllegalStateException("property '$propertyName' not found")
}