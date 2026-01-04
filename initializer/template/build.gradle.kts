plugins {
    <#if programmingLanguage == "JAVA">
        java
    </#if>
    <#if programmingLanguage == "GROOVY">
        groovy
    </#if>
    <#if programmingLanguage == "KOTLIN">
        kotlin("jvm") version "2.2.0"
        kotlin("kapt") version "2.2.0"
    </#if>

    id("me.vitalframework.vital-gradle-plugin") version "dev-SNAPSHOT"
}

group = "me.myproject"
version = "${version}"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    <#if programmingLanguage == "GROOVY">
        implementation("org.apache.groovy:groovy:5.0.0")
    </#if>

    <#if pluginEnvironment == "SPIGOT">
        compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")
    </#if>
    <#if pluginEnvironment == "PAPER">
        compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    </#if>
    <#if pluginEnvironment == "BUNGEE">
        compileOnly("net.md-5:bungeecord-api:1.21-R0.3")
    </#if>
}

<#if programmingLanguage == "GROOVY">
    tasks.compileGroovy {
        groovyOptions.isJavaAnnotationProcessing = true
    }
</#if>

<#if programmingLanguage == "KOTLIN">
    kotlin {
        jvmToolchain(24)
    }
</#if>
