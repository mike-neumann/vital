plugins {
    `maven-publish`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("vital-gradle-plugin") {
            id = "dev.vitalframework.vital-gradle-plugin"
            implementationClass = "dev.vitalframework.VitalGradlePlugin"
        }
    }
}

dependencies {
    api(project(":vital-core"))
    api(libs.bundles.gradlePlugin.api)
}

tasks.jar {
    manifest {
        attributes["Implementation-Version"] = project.version
    }
}
