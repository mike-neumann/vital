dependencies {
    implementation("me.xra1ny.essentia:essentia-configure:1.0")

    // may be contained in classpath
    compileOnly(project(":vital-holograms"))
    compileOnly(project(":vital-core"))
}