plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kapt)
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.boot)
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
        compileOnly(rootProject.libs.bundles.root.compileOnly)
        api(rootProject.libs.bundles.root.api)
        testImplementation(rootProject.libs.bundles.root.testImplementation)
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