dependencies {
    api(project(":vital-core"))
    api(project(":vital-cloudnet4-driver"))
    api("eu.cloudnetservice.cloudnet:bridge:${findProperty("cloudnetBridgeVersion")}")
}