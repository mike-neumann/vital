plugins {
    kotlin("jvm") version "1.9.24"
}
dependencies {
    api(project(":vital-core"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}