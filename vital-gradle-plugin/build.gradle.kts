plugins {
    `maven-publish`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("me.vitalframework.vital-gradle-plugin") {
            id = "me.vitalframework.vital-gradle-plugin"
            implementationClass = "me.vitalframework.VitalGradlePlugin"
        }
    }
}

dependencies {
    api(project(":vital-core"))
}