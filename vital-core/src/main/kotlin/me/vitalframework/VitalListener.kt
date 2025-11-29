package me.vitalframework

/**
 * A listener interface representing a framework component that can be registered to handle platform-specific events.
 * This interface is designed to support multiple server environments via platform-agnostic definitions.
 */
interface VitalListener {
    abstract class Spigot :
        VitalListener,
        SpigotListener

    abstract class Bungee :
        VitalListener,
        BungeeListener
}
