rootProject.name = "vital"

include("vital-cloudnet4-bridge")
include("vital-cloudnet4-driver")
include("vital-commands")
include("vital-commands-processor")
include("vital-configs")
include("vital-core")
include("vital-core-processor")
include("vital-gradle-plugin")
include("vital-holograms")
include("vital-inventories")
include("vital-items")
include("vital-minigames")
include("vital-players")
include("vital-scoreboards")
include("vital-statistics")
include("vital-tasks")
include("vital-utils")

gradle.beforeProject {
    // plugins
    extra["kotlinVersion"] = findProperty("kotlinVersion")
    extra["springBootPluginVersion"] = findProperty("springBootPluginVersion")
    extra["springDependencyManagementVersion"] = findProperty("springDependencyManagementVersion")
    // dependencies
    extra["spigotApiVersion"] = findProperty("spigotApiVersion")
    extra["brigadierVersion"] = findProperty("brigadierVersion")
    extra["bungeeApiVersion"] = findProperty("bungeeApiVersion")
    extra["junitVersion"] = findProperty("junitVersion")
}

pluginManagement {
    plugins {
        val kotlinVersion: String by extra
        val springBootPluginVersion: String by extra
        val springDependencyManagementVersion: String by extra

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootPluginVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
}