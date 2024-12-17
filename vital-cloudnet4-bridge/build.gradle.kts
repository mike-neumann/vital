plugins {
    kotlin("jvm") version "1.9.24"
}

dependencies {
    api(project(":vital-cloudnet4-driver"))
    api("eu.cloudnetservice.cloudnet:bridge:4.0.0-RC9")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}