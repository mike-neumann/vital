plugins {
    `maven-publish`
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.23"
}

group = "me.xra1ny"
version = "1.0"

gradlePlugin {
    plugins {
        create("vital-gradle-plugin") {
            id = "vital-gradle-plugin"
            implementationClass = "me.xra1ny.vital.VitalGradlePlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(21)
}