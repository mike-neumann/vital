package me.vitalframework

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails
import org.jetbrains.kotlin.konan.file.File
import org.springframework.boot.gradle.tasks.bundling.BootJar

class VitalGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        applyDefaultPlugins(target)
        fixTests(target)

        val vitalVersion = target.findProperty("vitalVersion")?.toString() ?: this::class.java.`package`.implementationVersion
        resolveVitalManagedDependencyVersions(target, vitalVersion)
        applyDefaultDependencies(target, vitalVersion)

        target.afterEvaluate {
            applyDefaultDependencies(it, vitalVersion)
            configureBootJar(it)
            fixLogging(it)
        }
    }

    private fun applyDefaultPlugins(project: Project) {
        // needs to be applied before dependency resolution takes place.
        project.plugins.apply("org.jetbrains.kotlin.plugin.spring")
        project.plugins.apply("io.spring.dependency-management")
        project.plugins.apply("org.springframework.boot")
    }

    private fun fixTests(project: Project) {
        // try to exclude the Vital core processor when running tests,
        // this will fix "NoMainClass" Exception while running tests.
        try {
            project.configurations.filter { "test" in it.name.lowercase() }.forEach {
                it.exclude(mapOf("group" to PACKAGE_VITAL, "module" to "vital-core-processor"))
            }
        } catch (_: Exception) {
            // if we don't have a test task, then we can ignore this step
        }
    }

    private fun resolveVitalManagedDependencyVersions(
        project: Project,
        vitalVersion: String,
    ) {
        // configure each dependency to use the same version as the plugin, so we don't have to define each version separately.
        // because of this implementation, we are able to just define the artifact without a version in our dependencies.
        project.configurations.all {
            it.resolutionStrategy.eachDependency {
                if (it.requested.version.isNullOrEmpty() && it.isVitalDependency()) {
                    it.useVersion(vitalVersion)
                    it.because("Managed by Vital Gradle Plugin")
                }
            }
        }
    }

    private fun applyDefaultDependencies(
        project: Project,
        vitalVersion: String,
    ) {
        project.applyDependency(
            "implementation",
            "$PACKAGE_VITAL:vital-core:$vitalVersion",
        )
        project.applyDependency(
            "kapt",
            "$PACKAGE_VITAL:vital-core-processor:$vitalVersion",
            "annotationProcessor",
            "$PACKAGE_VITAL:vital-core-processor:$vitalVersion",
        )

        if (project.hasDependency("$PACKAGE_VITAL:vital-commands")) {
            project.applyDependency(
                "kapt",
                "$PACKAGE_VITAL:vital-commands-processor:$vitalVersion",
                "annotationProcessor",
                "$PACKAGE_VITAL:vital-commands-processor:$vitalVersion",
            )
        }
    }

    private fun configureBootJar(project: Project) {
        // this builds the actual output jar, so it all fits nicely with the server's runtime (spigot, paper, bungee)
        project.tasks.named("bootJar", BootJar::class.java) { bootJar ->
            bootJar.mainClass.set("")
            bootJar.archiveFileName.set("${project.name}-old.jar")
            bootJar.outputs.dir("${DIR_TMP_UNPACKED}${File.separator}${DIR_BOOT_INF_CLASSES}")
            bootJar.doLast {
                val originalJar = bootJar.archiveFile.get().asFile
                val unpackDir =
                    project.layout.buildDirectory.dir(DIR_TMP_UNPACKED).get().asFile.apply {
                        deleteRecursively()
                        mkdirs()
                    }
                project.copy {
                    it.from(project.zipTree(originalJar))
                    it.into(unpackDir)
                }

                val bootInfLib = unpackDir.resolve(DIR_BOOT_INF_LIB)
                if (bootInfLib.exists()) {
                    for (file in bootInfLib.listFiles()!!) {
                        if (file.extension == "jar") {
                            val jarFiles = project.zipTree(file)
                            // kotlin and vital-loader dependencies MUST remain on the classpath, so the plugin can load
                            if (jarFiles.any { it.isKotlinPackage() || it.isVitalLoaderPackage() }) {
                                project.copy {
                                    it.from(jarFiles)
                                    it.into(unpackDir)
                                }

                                if (!jarFiles.any { it.isKotlinPackage() }) {
                                    // we want to keep kotlin dependencies as a jar on the root
                                    // otherwise we cannot use kotlin-reflect when our plugin tries to load
                                    // and start up the Vital instance
                                    continue
                                }
                            }
                        }

                        file.copyRecursively(unpackDir.resolve(file.name))
                    }
                    bootInfLib.deleteRecursively()
                }

                val bootInfClasses = unpackDir.resolve(DIR_BOOT_INF_CLASSES)
                if (bootInfClasses.exists()) {
                    for (file in bootInfClasses.listFiles()!!) {
                        file.copyRecursively(unpackDir.resolve(file.name))
                    }
                    bootInfClasses.deleteRecursively()
                }
                val repackedJar =
                    project.layout.buildDirectory
                        .file("${DIR_LIBS}${project.name}.jar")
                        .get()
                        .asFile
                project.ant.invokeMethod("zip", mapOf("basedir" to unpackDir, "destfile" to repackedJar))
            }
        }
    }

    private fun fixLogging(project: Project) {
        // also exclude logback, as this causes some problems with paper
        project.configurations.all {
            it.exclude(
                mapOf(
                    "group" to "ch.qos.logback",
                    "module" to "logback-core",
                    "group" to "ch.qos.logback",
                    "module" to "logback-classic",
                ),
            )
        }
    }

    private fun DependencyResolveDetails.isVitalDependency(): Boolean = requested.group.startsWith(PACKAGE_VITAL)

    private fun java.io.File.isKotlinPackage() = path.contains("kotlin${File.separator}")

    private fun java.io.File.isVitalLoaderPackage() = path.contains("me${File.separator}vitalframework${File.separator}loader")

    private fun Project.hasDependency(dependencyNotation: String) =
        configurations
            .flatMap { it.allDependencies }
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

    companion object {
        private const val PACKAGE_VITAL = "me.vitalframework"
        private val DIR_TMP_UNPACKED = "tmp${File.separator}unpacked"
        private val DIR_BOOT_INF_LIB = "BOOT-INF${File.separator}lib"
        private val DIR_BOOT_INF_CLASSES = "BOOT-INF${File.separator}classes"
        private val DIR_LIBS = "libs${File.separator}"
    }
}
