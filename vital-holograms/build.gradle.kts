dependencies {
    compileOnly(project(":vital-configs"))
    api(project(":vital-core"))
    api("net.kyori:adventure-text-minimessage:${properties["adventureTextMinimessageVersion"]}")
    api("net.kyori:adventure-text-serializer-legacy:${properties["adventureTextSerializerLegacyVersion"]}")
}