gradle.beforeProject {
    extra["adventureTextMinimessageVersion"] = findProperty("adventureTextMinimessageVersion")
    extra["adventureTextSerializerLegacyVersion"] = findProperty("adventureTextSerializerLegacyVersion")
    extra["adventureTextSerializerPlainVersion"] = findProperty("adventureTextSerializerPlainVersion")
}