plugins {
    `maven-publish`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("vital-gradle-plugin") {
            id = "me.vitalframework.vital-gradle-plugin"
            implementationClass = "me.vitalframework.VitalGradlePlugin"
        }
    }
}

dependencies {
    api(libs.bundles.gradlePlugin.api)
}