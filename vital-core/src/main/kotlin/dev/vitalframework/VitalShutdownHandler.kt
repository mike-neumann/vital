package dev.vitalframework

import dev.vitalframework.VitalCoreModule.Companion.logger
import net.md_5.bungee.api.event.ChatEvent
import org.bukkit.event.server.RemoteServerCommandEvent
import org.bukkit.event.server.ServerCommandEvent
import org.slf4j.Logger
import org.springframework.beans.factory.InitializingBean
import sun.misc.Signal

/**
 * Internal handler; responsible for gracefully shutting down Vital when server shutdown is requested.
 */
interface VitalShutdownHandler : InitializingBean {
    val logger: Logger
    val vitalPlugin: VitalPlugin

    override fun afterPropertiesSet() {
        Signal.handle(Signal("TERM")) {
            logger.info("Shutting down Vital via 'SIGTERM'")
            vitalPlugin.exit()
        }
        Signal.handle(Signal("INT")) {
            logger.info("Shutting down Vital via 'SIGINT'.")
            vitalPlugin.exit()
        }
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("Shutting down Vital via JVM shutdown hook.")
                vitalPlugin.exit()
            },
        )
    }

    class Spigot(
        override val vitalPlugin: VitalPlugin,
    ) : VitalListener.Spigot(),
        VitalShutdownHandler {
        override val logger = logger()

        @SpigotEventHandler
        fun onServerCommand(e: ServerCommandEvent) {
            if (!e.command.equals("stop", true)) {
                return
            }

            logger.info("Shutting down Vital via '/stop'.")
            vitalPlugin.exit()
        }

        @SpigotEventHandler
        fun onRemoteServerCommand(e: RemoteServerCommandEvent) {
            if (!e.command.equals("stop", true)) {
                return
            }

            logger.info("Shutting down Vital via RCON 'stop'.")
            vitalPlugin.exit()
        }
    }

    class Bungee(
        override val vitalPlugin: VitalPlugin,
    ) : VitalListener.Bungee(),
        VitalShutdownHandler {
        override val logger = logger()

        @BungeeEventHandler
        fun onChat(e: ChatEvent) {
            if (!e.message.equals("/end", true)) {
                return
            }

            logger.info("Shutting down Vital via '/end'.")
            vitalPlugin.exit()
        }
    }
}
