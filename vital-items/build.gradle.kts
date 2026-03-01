dependencies {
    compileOnly(project(":vital-localization"))
    compileOnlyApi(libs.bundles.items.compileOnlyApi)
    api(project(":vital-tasks"))
}
