group = "me.myproject"
version = "1.0.0"

plugins {
    // TODO: You can safely remove this, if you're using Kotlin.
    java

    // TODO: You can safely remove this, if you're not using Kotlin.
    kotlin("jvm") version "2.2.0"
    // TODO: You can safely remove this, if you're not using Kotlin.
    kotlin("kapt") version "2.2.0"

    // DO NOT REMOVE THIS!
    // This will automatically set everything up for your project, so you don't have to do it manually.
    id("me.vitalframework.vital-gradle-plugin") version "dev-SNAPSHOT"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // TODO: If you want to include more Vital submodules, like commands, etc.
    // You don't need to specify the version for any official Vital submodules, those are automatically managed by the Vital Gradle Plugin we defined above.
    implementation("me.vitalframework:vital-commands")
    implementation("me.vitalframework:vital-inventories")
    implementation("me.vitalframework:vital-items")
    implementation("me.vitalframework:vital-localization")
    implementation("me.vitalframework:vital-scoreboards")
}

// TODO: You can safely remove this, if you're not using Kotlin.
kotlin {
    jvmToolchain(24)
}