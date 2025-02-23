dependencies {
    api(project(":vital-cloudnet4-driver"))
    api("eu.cloudnetservice.cloudnet:bridge:${project.extra["cloudnetBridgeVersion"]}")
}