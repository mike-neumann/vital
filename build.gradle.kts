plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "2.1.0"
}

subprojects {
    group = "me.vitalframework"
    version = "1.0"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:${properties["spigotApiVersion"]}")
        compileOnly("com.mojang:brigadier:${properties["brigadierVersion"]}")
        compileOnly("net.md-5:bungeecord-api:${properties["bungeeApiVersion"]}")

        api("org.springframework.boot:spring-boot-starter:${properties["springBootVersion"]}")

        testApi("org.springframework.boot:spring-boot-starter-test:${properties["springBootVersion"]}")
        testApi("org.junit.jupiter:junit-jupiter-api:${properties["junitVersion"]}")
        testApi("org.spigotmc:spigot-api:${properties["spigotApiVersion"]}")
        testApi("com.mojang:brigadier:${properties["brigadierVersion"]}")
        testApi("net.md-5:bungeecord-api:${properties["bungeeApiVersion"]}")
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