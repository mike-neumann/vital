import org.codehaus.groovy.runtime.ProcessGroovyMethods

fun getGitTag(): String {
    val tag = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git tag --points-at HEAD"))
    return tag
        .trim()
        .let { if (it.startsWith("v")) it.substring(1) else it }
        // if no tag is detected, we are running a dev build / not an officially released version
        .ifBlank { "dev-SNAPSHOT" }
}

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.javaLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenLocal()
    mavenCentral()
}

subprojects {
    group = "me.vitalframework"
    version = getGitTag()

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
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
            maven("http://10.8.0.1:8082/repository/maven-releases/") {
                isAllowInsecureProtocol = true
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
}
