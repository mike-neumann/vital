dependencies {
    compileOnly(project(":vital-configs"))
    compileOnlyApi(libs.bundles.holograms.compileOnlyApi)
    api(project(":vital-core"))
}
