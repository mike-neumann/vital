package me.vitalframework.commands

import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlugin
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component
import java.lang.reflect.InvocationTargetException

class VitalCommandsSubModule {
    @RequiresSpigot
    @Component("vital-commands")
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

    @RequiresBungee
    @Component("vital-commands")
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
