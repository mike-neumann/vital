plugins {
    kotlin("jvm") version "1.9.24"
}

dependencies {
    api("eu.cloudnetservice.cloudnet:driver:4.0.0-RC9")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}