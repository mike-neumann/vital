package me.vitalframework

/**
 * Defines a listener within the Vital-Framework.
 * Check the specific platform-implementation for an example on how to implement it.
 */
interface VitalListener {
    /**
     * Defines a Spigot listener within the Vital-Framework.
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
    abstract class Spigot :
        VitalListener,
        SpigotListener

    /**
     * Defines a BungeeCord-Listener within the Vital-Framework.
     *
     * ```java
     * @Listener
     * public class MyListener extends VitalListener.Bungee {
     *   @EventHandler
     *   public void onLogin(LoginEvent e) {
     *     // ...
     *   }
     *
     *   // ...
     * }
     * ```
     */
    abstract class Bungee :
        VitalListener,
        BungeeListener
}
