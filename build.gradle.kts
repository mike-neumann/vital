plugins {
    `java-library`
    `maven-publish`
}

subprojects {
    group = "me.vitalframework"
    version = "1.0"

    apply<MavenPublishPlugin>()
    apply<JavaLibraryPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        annotationProcessor("org.projectlombok:lombok:1.18.32")

        compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
        compileOnly("com.mojang:brigadier:1.2.9")

        compileOnly("org.projectlombok:lombok:1.18.32")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")

        api("org.springframework.boot:spring-boot-starter:3.3.1")
        api("jakarta.annotation:jakarta.annotation-api:3.0.0")
        api("org.reflections:reflections:0.10.2")
    }

//    java {
//        withSourcesJar()
//        withJavadocJar()
//    }

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
        (options as StandardJavadocDocletOptions)
            .tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
    }
}