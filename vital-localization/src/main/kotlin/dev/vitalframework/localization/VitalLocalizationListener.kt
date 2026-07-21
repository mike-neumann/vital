package dev.vitalframework.localization

import dev.vitalframework.BungeeEventHandler
import dev.vitalframework.BungeeEventPriority
import dev.vitalframework.Listener
import dev.vitalframework.RequiresBungee
import dev.vitalframework.RequiresSpigot
import dev.vitalframework.SpigotEventHandler
import dev.vitalframework.SpigotEventPriority
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.context.annotation.Conditional

/**
 * Internal listener; handles localization cleanup when a player leaves the server.
 */
interface VitalLocalizationListener {
    fun handle(player: Any) {
        VitalLocalizationModule.playerLocales.remove(player)
    }

    @Conditional(RequiresSpigot::class)
    @Listener
    class Spigot : VitalLocalizationListener {
        @SpigotEventHandler(SpigotEventPriority.HIGHEST)
        fun onPlayerQuit(e: PlayerQuitEvent) {
            handle(e.player)
        }
    }

    @Conditional(RequiresBungee::class)
    @Listener
    class Bungee : VitalLocalizationListener {
        @BungeeEventHandler(BungeeEventPriority.HIGHEST)
        fun onPlayerDisconnect(e: PlayerDisconnectEvent) {
            handle(e.player)
        }
    }
}
