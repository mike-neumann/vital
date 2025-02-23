dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-text-minimessage:${project.extra["adventureTextMinimessageVersion"]}")
    api("net.kyori:adventure-text-serializer-legacy:${project.extra["adventureTextSerializerLegacyVersion"]}")
}