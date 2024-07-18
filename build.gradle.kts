plugins {
    java
    `maven-publish`
}

subprojects {
    group = "me.xra1ny.vital"
    version = "1.0"

    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        annotationProcessor("org.projectlombok:lombok:1.18.32")
        compileOnly("org.projectlombok:lombok:1.18.32")
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
        compileOnly("org.springframework.boot:spring-boot-starter:3.3.1")

        implementation("org.reflections:reflections:0.10.2")
        implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    }

    java {
        withSourcesJar()
        withJavadocJar()
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
        (options as StandardJavadocDocletOptions)
            .tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
    }
}