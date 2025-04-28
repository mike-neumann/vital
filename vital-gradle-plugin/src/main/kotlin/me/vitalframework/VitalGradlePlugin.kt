package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.file.File
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
                    val unpackDir = target.layout.buildDirectory.dir(DIR_TMP_UNPACKED).get().asFile.apply {
                        deleteRecursively()
                        mkdirs()
                    }
                    target.copy {
                        it.from(target.zipTree(originalJar))
                        it.into(unpackDir)
                    }
                    val bootInfLib = unpackDir.resolve(DIR_BOOT_INF_LIB)
                    if (bootInfLib.exists()) {
                        for (file in bootInfLib.listFiles()!!) {
                            if (file.extension == "jar") {
                                val jarFiles = target.zipTree(file)
                                // kotlin and vital-loader dependencies MUST remain on the classpath, so the plugin can load
                                if (jarFiles.any { it.isKotlinPackage() || it.isVitalLoaderPackage() }) {
                                    target.copy {
                                        it.from(jarFiles)
                                        it.into(unpackDir)
                                    }

                                    if (!jarFiles.any { it.isKotlinPackage() }) {
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
                    val bootInfClasses = unpackDir.resolve(DIR_BOOT_INF_CLASSES)
                    if (bootInfClasses.exists()) {
                        for (file in bootInfClasses.listFiles()!!) {
                            file.renameTo(unpackDir.resolve(file.name))
                        }
                        bootInfClasses.deleteRecursively()
                    }
                    val repackedJar = target.layout.buildDirectory.file("${DIR_LIBS}${target.name}.jar").get().asFile
                    target.ant.invokeMethod("zip", mapOf("destfile" to repackedJar, "basedir" to unpackDir))
                }
            }
        }
    }

    private fun java.io.File.isKotlinPackage() = path.contains("kotlin${File.separator}")
    private fun java.io.File.isVitalLoaderPackage() = path.contains("me${File.separator}vitalframework${File.separator}loader")

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

    companion object {
        private val DIR_TMP_UNPACKED = "tmp${File.separator}unpacked"
        private val DIR_BOOT_INF_LIB = "BOOT-INF${File.separator}lib"
        private val DIR_BOOT_INF_CLASSES = "BOOT-INF${File.separator}classes"
        private val DIR_LIBS = "libs${File.separator}"
    }
}