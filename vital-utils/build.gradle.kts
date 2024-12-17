plugins {
    kotlin("jvm") version "1.9.24"
}

dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-platform-bungeecord:4.3.2")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    api("net.kyori:adventure-text-serializer-legacy:4.17.0")
    api("net.kyori:adventure-platform-bukkit:4.3.2")
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}