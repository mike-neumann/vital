plugins {
    `maven-publish`
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.24"
}

gradlePlugin {
    plugins {
        create("me.vitalframework.vital-gradle-plugin") {
            id = "me.vitalframework.vital-gradle-plugin"
            implementationClass = "me.vitalframework.VitalGradlePlugin"
        }
    }
}

dependencies {
    api(project(":vital-core"))
}

kotlin {
    jvmToolchain(21)
}