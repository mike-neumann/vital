dependencies {
    api(project(":vital-core"))
    api("net.kyori:adventure-platform-bungeecord:${project.extra["adventurePlatformBungeecordVersion"]}")
    api("net.kyori:adventure-text-minimessage:${project.extra["adventureTextMinimessageVersion"]}")
    api("net.kyori:adventure-text-serializer-legacy:${project.extra["adventureTextSerializerLegacyVersion"]}")
    api("net.kyori:adventure-platform-bukkit:${project.extra["adventurePlatformBukkitVersion"]}")
}