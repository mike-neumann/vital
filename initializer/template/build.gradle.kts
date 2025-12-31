plugins {
    {plugins}

    id("me.vitalframework.vital-gradle-plugin") version "dev-SNAPSHOT"
}

group = "me.myproject"
version = "{version}"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    {pluginEnvironment}
    {additionalDependencies}
}

{additionalConfigurations}