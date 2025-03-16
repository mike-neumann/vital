dependencies {
    api(project(":vital-tasks"))
    api("net.kyori:adventure-text-minimessage:${findProperty("adventureTextMinimessageVersion")}")
    api("net.kyori:adventure-text-serializer-legacy:${findProperty("adventureTextSerializerLegacyVersion")}")
}