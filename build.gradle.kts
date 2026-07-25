import org.codehaus.groovy.runtime.ProcessGroovyMethods
import java.io.IOException

fun getGitTag(): String {
    try {
        val tag = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git tag --points-at HEAD"))
        return tag
            .trim()
            .let { if (it.startsWith("v")) it.substring(1) else it }
            // if no tag is detected, we are running a dev build / not an officially released version
            .ifBlank { "dev-SNAPSHOT" }
    } catch (_: IOException) {
        logger.warn(
            "Failed to extract Vital version from git tag. This could be because git is not installed on this system. Will fall back to 'dev-SNAPSHOT'.",
        )
        return "dev-SNAPSHOT"
    }
}

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.javaLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dokka {
    pluginsConfiguration.html {
        separateInheritedMembers.set(false)
        mergeImplicitExpectActualDeclarations.set(true)
    }
}

dependencies {
    dokka(project(":vital-cloudnet4-bridge"))
    dokka(project(":vital-cloudnet4-driver"))
    dokka(project(":vital-commands"))
    dokka(project(":vital-commands-processor"))
    dokka(project(":vital-configs"))
    dokka(project(":vital-core"))
    dokka(project(":vital-gradle-plugin"))
    dokka(project(":vital-core-processor"))
    dokka(project(":vital-holograms"))
    dokka(project(":vital-inventories"))
    dokka(project(":vital-items"))
    dokka(project(":vital-loader"))
    dokka(project(":vital-localization"))
    dokka(project(":vital-minigames"))
    dokka(project(":vital-players"))
    dokka(project(":vital-scoreboards"))
    dokka(project(":vital-statistics"))
    dokka(project(":vital-tasks"))
    dokka(project(":vital-tests"))
    dokka(project(":vital-utils"))
}

allprojects {
    tasks.findByName("bootJar")?.enabled = false
}

subprojects {
    group = "dev.vitalframework"
    version = getGitTag()

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.dokka-javadoc")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://libraries.minecraft.net")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly(rootProject.libs.bundles.root.compileOnly)
        api(rootProject.libs.bundles.root.api)
        testImplementation(rootProject.libs.bundles.root.testImplementation)
        testImplementation(rootProject.libs.bundles.tests.api)
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    kotlin {
        jvmToolchain(24)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = group.toString()
                artifactId = project.name
                version = version
                from(components["java"])
            }
        }

        repositories {
            // TODO: this solution is temporary, so i can pull Vital for my own projects
            // TODO: release Vital to maven central once i have a stable version
            if (version.toString().endsWith("-SNAPSHOT")) {
                maven("http://repo.rainymc.de/nexus/content/repositories/snapshots/") {
                    isAllowInsecureProtocol = true
                }
            } else {
                maven("http://repo.rainymc.de/nexus/content/repositories/releases/") {
                    isAllowInsecureProtocol = true
                }
            }
        }
    }

    configurations.all {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "ch.qos.logback", module = "logback-core")
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "org.slf4j", module = "jul-to-slf4j")
        exclude(group = "org.slf4j", module = "log4j-over-slf4j")
    }

    tasks.compileKotlin {
        // so default impls of interfaces work across multiplatform compiled kotlin code (kotlin >> java)
        compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
    }

    tasks.javadoc {
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:",
        )
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.findByName("bootJar")?.enabled = false
}
