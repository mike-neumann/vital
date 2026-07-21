package dev.vitalframework

/**
 * Defines a listener within the Vital-Framework.
 *
 * ```java
 * @Listener
 * public class MyListener extends VitalListener.Spigot {
 *   @EventHandler
 *   public void onPlayerJoin(PlayerJoinEvent e) {
 *     // ...
 *   }
 *
 *   // ...
 * }
 * ```
 */
interface VitalListener {
    abstract class Spigot :
        VitalListener,
        SpigotListener

    abstract class Bungee :
        VitalListener,
        BungeeListener
}
