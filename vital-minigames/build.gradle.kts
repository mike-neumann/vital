plugins {
    kotlin("jvm") version "1.9.24"
}

dependencies {
    api(project(":vital-tasks"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}