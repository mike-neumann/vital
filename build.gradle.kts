plugins {
    java
    `maven-publish`
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    compileOnly(project(":vital-core"))
    implementation(project(":vital-core-processor"))
    implementation(project(":vital-commands-processor"))
}

allprojects {
    group = "me.xra1ny.vital"
    version = "1.0"

    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
        implementation("org.reflections:reflections:0.10.2")
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")

        implementation("me.xra1ny.essentia:essentia-except:1.0")
        implementation("org.springframework:spring-context:6.1.10")
        implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
        implementation("org.springframework.boot:spring-boot-starter")
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