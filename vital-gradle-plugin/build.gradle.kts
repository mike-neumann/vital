plugins {
    `maven-publish`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("vital-gradle-plugin") {
            id = "me.vitalframework.vital-gradle-plugin"
            implementationClass = "me.vitalframework.VitalGradlePlugin"
        }
    }
}

dependencies {
    api(project(":vital-core"))
    api("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:${findProperty("kotlinVersion")}")
    api("io.spring.gradle:dependency-management-plugin:${findProperty("dependencyManagementPluginVersion")}")
    api("org.springframework.boot:org.springframework.boot.gradle.plugin:${findProperty("springBootVersion")}")
}