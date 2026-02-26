package me.vitalframework.localization

import me.vitalframework.BungeeEventHandler
import me.vitalframework.BungeeEventPriority
import me.vitalframework.Listener
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotEventHandler
import me.vitalframework.SpigotEventPriority
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.springframework.context.annotation.Conditional

interface VitalLocalizationListener {
    fun handle(player: Any) {
        VitalLocalizationSubModule.playerLocales.remove(player)
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
