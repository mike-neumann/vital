package me.vitalframework

/**
 * Defines different plugin environments needed for automatic plugin configuration file creation
 */
enum class VitalPluginEnvironment(
    val ymlFileName: String,
) {
    SPIGOT("plugin.yml"),
    BUNGEE("bungee.yml")
}