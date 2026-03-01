dependencies {
    compileOnly(project(":vital-localization"))
    compileOnlyApi(libs.bundles.inventories.compileOnlyApi)
    api(project(":vital-core"))
    api(project(":vital-items"))
}
