plugins {
    kotlin("jvm") version "1.9.24"
}
dependencies {
    api(project(":vital-core"))
    api("commons-io:commons-io:2.16.1")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}