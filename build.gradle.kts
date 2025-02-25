plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `java-library`
    `maven-publish`
}

subprojects {
    group = "me.vitalframework"
    version = "1.0"

    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:${project.extra["spigotApiVersion"]}")
        compileOnly("com.mojang:brigadier:${project.extra["brigadierVersion"]}")
        compileOnly("net.md-5:bungeecord-api:${project.extra["bungeeApiVersion"]}")

        api("org.springframework.boot:spring-boot-starter:${project.extra["springBootPluginVersion"]}") {
            exclude(group = "org.apache.hc.client5")
        }
        api("org.apache.httpcomponents.client5:httpclient5:5.4.2")

        testApi("org.springframework.boot:spring-boot-starter-test:${project.extra["springBootPluginVersion"]}")
        testApi("org.junit.jupiter:junit-jupiter-api:${project.extra["junitVersion"]}")
        testApi("org.spigotmc:spigot-api:${project.extra["spigotApiVersion"]}")
        testApi("com.mojang:brigadier:${project.extra["brigadierVersion"]}")
        testApi("net.md-5:bungeecord-api:${project.extra["bungeeApiVersion"]}")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.compileKotlin {
        // so default impls of interfaces work across multiplatform compiled kotlin code (kotlin >> java)
        compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
    }

    kotlin {
        jvmToolchain(21)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = group.toString()
                artifactId = project.name
                version = version

                from(components["java"])
            }
        }
    }

    tasks.javadoc {
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    tasks.test {
        useJUnitPlatform()
    }
}