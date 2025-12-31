import org.codehaus.groovy.runtime.ProcessGroovyMethods

plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "me.vitalframework"
version = getGitTag()

fun getGitTag(): String {
    val tag = ProcessGroovyMethods.getText(ProcessGroovyMethods.execute("git tag --points-at HEAD"))
    return tag
        .trim()
        .let { if (it.startsWith("v")) it.substring(1) else it }
        // if no tag is detected, we are running a dev build / not an officially released version
        .ifBlank { "dev-SNAPSHOT" }
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(24)
}
