dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-text-minimessage:${findProperty("adventureTextMinimessageVersion")}")
    api("net.kyori:adventure-text-serializer-legacy:${findProperty("adventureTextSerializerLegacyVersion")}")
    api("net.kyori:adventure-text-serializer-plain:${findProperty("adventureTextSerializerPlainVersion")}")
}