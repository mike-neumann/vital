package me.vitalframework.commands

import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlugin
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotPlugin
import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException

/**
 * Defines the official vital-commands submodule.
 * This class defines each platform-specific submodule, which is displayed when Vital starts.
 *
 * Each platform specific implementation contains the Vital commands system to create advanced commands,
 * in a declarative annotation-based way.
 * It enables typesafe commands with an easy-to-read API.
 */
class VitalCommandsSubModule {
    /**
     * Defines the official Spigot vital-commands submodule, which is displayed when Vital starts.
     */
    @RequiresSpigot
    @SubModule("vital-commands")
    class Spigot(
        val plugin: SpigotPlugin,
        val vitalCommands: List<VitalCommand.Spigot>,
    ) : VitalSubModule() {
        val logger = logger()

        override fun onInstall() {
            try {
                Class.forName("org.bukkit.Bukkit")
            } catch (_: Exception) {
                logger.error(
                    "'vital-commands' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
                )
                logger.error(
                    "Please make sure you are running 'vital-commands' in the correct server environment, e.g. Spigot, Paper, Bungee.",
                )
            }

            for (vitalCommand in vitalCommands) {
                try {
                    plugin.getCommand(vitalCommand.name)!!.setExecutor(vitalCommand)
                    logger.info("Spigot command '${vitalCommand::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    logger.error("Error while registering spigot command '${vitalCommand::class.java.name}'", e)
                }
            }
        }
    }

    /**
     * Defines the official BungeeCord vital-commands submodule, which is displayed when Vital starts.
     */
    @RequiresBungee
    @SubModule("vital-commands")
    class Bungee(
        val plugin: BungeePlugin,
        val vitalCommands: List<VitalCommand.Bungee>,
    ) : VitalSubModule() {
        val logger = logger()

        override fun onInstall() {
            for (vitalCommand in vitalCommands) {
                try {
                    Class.forName("net.md_5.bungee.api.ProxyServer")
                } catch (_: Exception) {
                    logger.error(
                        "'vital-commands' has been installed, but the BungeeCord runtime was not found on the server classpath, calling BungeeCord APIs might fail.",
                    )
                    logger.error(
                        "Please make sure you are running 'vital-commands' in the correct server environment, e.g. Spigot, Paper, Bungee.",
                    )
                }

                try {
                    val command =
                        object : VitalPluginCommand.Bungee(vitalCommand.name) {
                            override fun execute(
                                sender: BungeeCommandSender,
                                args: Array<String>,
                            ) {
                                vitalCommand.execute(sender, args)
                            }

                            override fun onTabComplete(
                                sender: BungeeCommandSender,
                                args: Array<String>,
                            ) = vitalCommand.tabComplete(sender, args)
                        }
                    plugin.proxy.pluginManager.registerCommand(plugin, command)

                    logger.info("Bungee command '${vitalCommand::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    logger.error("Error while registering bungee command '${vitalCommand::class.java.name}'", e)
                }
            }
        }
    }

    companion object {
        fun Throwable.extractNonInvocationTargetException(): Throwable {
            var exception = this
            if (exception is InvocationTargetException) {
                var extractedException = targetException
                while (extractedException is InvocationTargetException) {
                    extractedException = extractedException.targetException
                }

                exception = extractedException
            }

            return exception
        }
    }
}
