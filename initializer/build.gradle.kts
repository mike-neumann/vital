import org.codehaus.groovy.runtime.ProcessGroovyMethods
import java.io.IOException

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
}

group = "me.vitalframework"
version = getGitTag()

fun getGitTag(): String {
    try {
        val tag = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git tag --points-at HEAD"))
        return tag
            .trim()
            .let { if (it.startsWith("v")) it.substring(1) else it }
            // if no tag is detected, we are running a dev build / not an officially released version
            .ifBlank { "dev-SNAPSHOT" }
    } catch (_: IOException) {
        logger.warn("Failed to extract Vital initializer version from git tag. This could be because git is not installed on this system. Will fall back to 'dev-SNAPSHOT'.")
        return "dev-SNAPSHOT"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.implementation)
    testImplementation(libs.bundles.testImplementation)
}

kotlin {
    jvmToolchain(24)
}

application {
    mainClass = "me.vitalframework.initializer.VitalInitializerKt"
}

tasks.run {
    workingDir = projectDir
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}
