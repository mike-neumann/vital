plugins {
    java
    id("me.vitalframework.vital-gradle-plugin") version "dev-SNAPSHOT"
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
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
}
