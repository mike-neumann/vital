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
        compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
        compileOnly("com.mojang:brigadier:1.2.9")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")

        api("org.springframework.boot:spring-boot-starter:3.3.1")

        testApi("org.springframework.boot:spring-boot-starter-test:3.3.1")
        testApi("org.junit.jupiter:junit-jupiter-api:5.11.4")
        testApi("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
        testApi("com.mojang:brigadier:1.2.9")
        testApi("net.md-5:bungeecord-api:1.20-R0.2")
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