dependencies {
    api(project(":vital-loader"))
    api("org.reflections:reflections:${findProperty("reflectionsVersion")}")
    api("org.springframework.boot:spring-boot-loader:${findProperty("springBootVersion")}")
    api("org.springframework.boot:spring-boot-loader-tools:${findProperty("springBootVersion")}")
}