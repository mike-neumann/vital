plugins {
    kotlin("jvm") version "1.9.24"
}

dependencies {
    // may be contained in classpath
    compileOnly(project(":vital-holograms"))

    api(project(":vital-core"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}