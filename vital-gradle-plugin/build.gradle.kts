plugins {
    `maven-publish`
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.23"
}

gradlePlugin {
    plugins {
        create("me.xra1ny.vital.vital-gradle-plugin") {
            id = "me.xra1ny.vital.vital-gradle-plugin"
            implementationClass = "me.xra1ny.vital.VitalGradlePlugin"
        }
    }
}

dependencies {
    api(project(":vital-core"))
}

kotlin {
    jvmToolchain(21)
}