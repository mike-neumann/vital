dependencies {
    compileOnly(project(":vital-configs"))
    api(project(":vital-core"))
    api("net.kyori:adventure-text-minimessage:${findProperty("adventureTextMinimessageVersion")}")
    api("net.kyori:adventure-text-serializer-legacy:${findProperty("adventureTextSerializerLegacyVersion")}")
}