dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-platform-bungeecord:${findProperty("adventurePlatformBungeecordVersion")}")
    api("net.kyori:adventure-text-minimessage:${findProperty("adventureTextMinimessageVersion")}")
    api("net.kyori:adventure-text-serializer-legacy:${findProperty("adventureTextSerializerLegacyVersion")}")
    api("net.kyori:adventure-platform-bukkit:${findProperty("adventurePlatformBukkitVersion")}")
}