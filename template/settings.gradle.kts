// TODO: This will define the name of your project, change it to your plugin's name.
rootProject.name = "template"

// This right here is important.
// It will configure where Gradle will look for plugins.
// As Vital is currently not in any central plugin portal,
// we need to tell Gradle that we want to use the local repository,
// as well as the central Gradle plugin portal to find our plugins.
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}