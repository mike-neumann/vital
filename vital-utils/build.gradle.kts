dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-text-minimessage:${findProperty("adventureTextSerializerVersion")}")
    api("net.kyori:adventure-text-serializer-legacy:${findProperty("adventureTextSerializerVersion")}")
    api("net.kyori:adventure-platform-bungeecord:${findProperty("adventurePlatformVersion")}")
    api("net.kyori:adventure-platform-bukkit:${findProperty("adventurePlatformVersion")}")
}