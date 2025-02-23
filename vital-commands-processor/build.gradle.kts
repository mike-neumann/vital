dependencies {
    api(project(":vital-core-processor"))
    api(project(":vital-commands"))
    api("org.reflections:reflections:${project.extra["reflectionsVersion"]}")
}