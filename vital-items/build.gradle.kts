dependencies {
    api(project(":vital-tasks"))
    api("net.kyori:adventure-text-minimessage:${properties["adventureTextMinimessageVersion"]}")
    api("net.kyori:adventure-text-serializer-legacy:${properties["adventureTextSerializerLegacyVersion"]}")
}