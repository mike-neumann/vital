plugins {
    java
    `maven-publish`
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

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
        implementation("org.reflections:reflections:0.10.2")
        implementation("org.slf4j:slf4j-api:2.0.13")
        implementation("ch.qos.logback:logback-classic:1.5.6")
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")

        // needed for dependency injection.
        implementation("me.xra1ny.essentia:essentia-inject:1.0")
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