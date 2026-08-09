plugins {
    java
    id("dev.vitalframework.vital-gradle-plugin") version "dev-SNAPSHOT"
}

group = "me.myproject"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.3")
}

