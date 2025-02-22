dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-platform-bungeecord:${properties["adventurePlatformBungeecordVersion"]}")
    api("net.kyori:adventure-text-minimessage:${properties["adventureTextMinimessageVersion"]}")
    api("net.kyori:adventure-text-serializer-legacy:${properties["adventureTextSerializerLegacyVersion"]}")
    api("net.kyori:adventure-platform-bukkit:${properties["adventurePlatformBukkitVersion"]}")
}