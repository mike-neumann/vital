gradle.beforeProject {
    extra["adventurePlatformBungeecordVersion"] = findProperty("adventurePlatformBungeecordVersion")
    extra["adventureTextMinimessageVersion"] = findProperty("adventureTextMinimessageVersion")
    extra["adventureTextSerializerLegacyVersion"] = findProperty("adventureTextSerializerLegacyVersion")
    extra["adventurePlatformBukkitVersion"] = findProperty("adventurePlatformBukkitVersion")
}