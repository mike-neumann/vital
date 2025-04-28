plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    `java-library`
    `maven-publish`
}

allprojects {
    group = "me.vitalframework"
    version = "1.0"

    apply(plugin = "kotlin")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://libraries.minecraft.net")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:${findProperty("paperApiVersion")}")
        compileOnly("com.mojang:brigadier:${findProperty("brigadierVersion")}")
        compileOnly("net.md-5:bungeecord-api:${findProperty("bungeeApiVersion")}")

        api("org.springframework.boot:spring-boot-starter:${findProperty("springBootVersion")}")

        testImplementation("org.springframework.boot:spring-boot-starter-test:${findProperty("springBootVersion")}")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${findProperty("junitVersion")}")
        testImplementation("io.papermc.paper:paper-api:${findProperty("paperApiVersion")}")
        testImplementation("com.mojang:brigadier:${findProperty("brigadierVersion")}")
        testImplementation("net.md-5:bungeecord-api:${findProperty("bungeeApiVersion")}")
    }

    java {
        withSourcesJar()
        withJavadocJar()
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

    tasks.compileKotlin {
        // so default impls of interfaces work across multiplatform compiled kotlin code (kotlin >> java)
        compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
    }

    tasks.javadoc {
        (options as StandardJavadocDocletOptions).tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }

    tasks.test { useJUnitPlatform() }
}