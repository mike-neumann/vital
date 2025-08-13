dependencies {
    api(project(":vital-core"))
    api(project(":vital-cloudnet4-driver"))
    compileOnly(libs.bundles.cloudnet4.driverApi.compileOnly)
    compileOnly(libs.bundles.cloudnet4.bridgeApi.compileOnly)
}
