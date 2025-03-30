dependencies {
    api(project(":vital-tasks"))
    api("net.kyori:adventure-text-minimessage:${findProperty("adventureTextSerializerVersion")}")
    api("net.kyori:adventure-text-serializer-legacy:${findProperty("adventureTextSerializerVersion")}")
}