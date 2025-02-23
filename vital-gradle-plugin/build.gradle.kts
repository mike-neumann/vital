plugins {
    `maven-publish`
    `java-gradle-plugin`
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
    api("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:${project.extra["kotlinSpringPluginVersion"]}")
    api("io.spring.gradle:dependency-management-plugin:${project.extra["dependencyManagementPluginVersion"]}")
    api("org.springframework.boot:org.springframework.boot.gradle.plugin:${project.extra["springBootPluginVersion"]}")
    api("com.gradleup.shadow:shadow-gradle-plugin:${project.extra["shadowVersion"]}")
}