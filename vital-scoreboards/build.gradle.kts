dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-text-minimessage:${properties["adventureTextMinimessageVersion"]}")
    api("net.kyori:adventure-text-serializer-legacy:${properties["adventureTextSerializerLegacyVersion"]}")
    api("net.kyori:adventure-text-serializer-plain:${properties["adventureTextSerializerPlainVersion"]}")
}