package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.springframework.boot.gradle.tasks.bundling.BootJar

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // needs to be applied before dependency resolution takes place.
        target.plugins.apply("org.springframework.boot")
        target.plugins.apply("org.jetbrains.kotlin.plugin.spring")
        target.plugins.apply("io.spring.dependency-management")

        target.afterEvaluate {
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

            target.tasks.named("bootJar", BootJar::class.java) { bootJar ->
                bootJar.mainClass.set("")
                bootJar.archiveFileName.set("${target.name}-old.jar")
                bootJar.doLast {
                    val originalJar = bootJar.archiveFile.get().asFile
                    val unpackDir = target.layout.buildDirectory.dir("tmp/unpacked").get().asFile.apply {
                        deleteRecursively()
                        mkdirs()
                    }
                    target.copy {
                        it.from(target.zipTree(originalJar))
                        it.into(unpackDir)
                    }
                    val bootInfLib = unpackDir.resolve("BOOT-INF/lib")
                    if (bootInfLib.exists()) {
                        for (file in bootInfLib.listFiles()!!) {
                            if (file.extension == "jar") {
                                val jarFiles = target.zipTree(file)

                                //
                                if (jarFiles.any { it.path.contains("kotlin/") || it.path.contains("me/vitalframework/loader") }) {
                                    target.copy {
                                        it.from(jarFiles)
                                        it.into(unpackDir)
                                    }

                                    if (!jarFiles.any { it.path.contains("kotlin/") }) {
                                        // we want to keep kotlin dependencies as a jar on the root
                                        // otherwise we cannot use kotlin-reflect when our plugin tries to load
                                        // and start up the vital instance
                                        continue
                                    }
                                }
                            }

                            file.renameTo(unpackDir.resolve(file.name))
                        }
                        bootInfLib.deleteRecursively()
                    }
                    val bootInfClasses = unpackDir.resolve("BOOT-INF/classes")
                    if (bootInfClasses.exists()) {
                        for (file in bootInfClasses.listFiles()!!) {
                            file.renameTo(unpackDir.resolve(file.name))
                        }
                        bootInfClasses.deleteRecursively()
                    }
                    val repackedJar = target.layout.buildDirectory.file("libs/${target.name}.jar").get().asFile
                    target.ant.invokeMethod("zip", mapOf("destfile" to repackedJar, "basedir" to unpackDir))
                }
            }
        }
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

    private fun Project.getProperty(propertyName: String) = findProperty(propertyName)
        ?: throw IllegalStateException("property '$propertyName' not found")
}