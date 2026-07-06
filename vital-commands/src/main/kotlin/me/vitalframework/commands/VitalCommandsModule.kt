package me.vitalframework.commands

import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlugin
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotPlugin
import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional

@VitalModule.Info(value = "vital-commands")
class VitalCommandsModule {
    @ConditionalOnMissingBean
    @Bean
    fun vitalGlobalCommandExceptionHandlerProcessor(
        applicationContext: ApplicationContext,
        vitalCommands: List<VitalCommand<*>>,
    ) = VitalGlobalCommandExceptionHandlerProcessor(applicationContext, vitalCommands)

    @Conditional(RequiresSpigot::class)
    @VitalModule.Info(value = "vital-commands.spigot")
    class Spigot(
        val plugin: SpigotPlugin,
        val vitalCommands: List<VitalCommand.Spigot>,
    ) : VitalModule() {
        val logger = logger()

        override fun onEnable() {
            try {
                Class.forName("org.bukkit.Bukkit")
            } catch (_: Exception) {
                logger.error(
                    "'vital-commands' for Spigot has been enabled, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
                )
                logger.error(
                    "Please make sure you are running 'vital-commands' in the correct server environment, e.g. Spigot, Paper, Bungee.",
                )
            }

            for (vitalCommand in vitalCommands) {
                try {
                    val info = vitalCommand.getInfo(VitalCommand.Info::class.java)
                    plugin.getCommand(info.name)!!.setExecutor(vitalCommand)
                    logger.info("Spigot command '${vitalCommand::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    logger.error("Error while registering spigot command '${vitalCommand::class.java.name}'", e)
                }
            }
        }
    }

    @Conditional(RequiresBungee::class)
    @VitalModule.Info(value = "vital-commands.bungee")
    class Bungee(
        val plugin: BungeePlugin,
        val vitalCommands: List<VitalCommand.Bungee>,
    ) : VitalModule() {
        val logger = logger()

        override fun onEnable() {
            for (vitalCommand in vitalCommands) {
                try {
                    Class.forName("net.md_5.bungee.api.ProxyServer")
                } catch (_: Exception) {
                    logger.error(
                        "'vital-commands' for BungeeCord has been enabled, but the BungeeCord runtime was not found on the server classpath, calling BungeeCord APIs might fail.",
                    )
                    logger.error(
                        "Please make sure you are running 'vital-commands' in the correct server environment, e.g. Spigot, Paper, Bungee.",
                    )
                }

                try {
                    val info = vitalCommand.getInfo(VitalCommand.Info::class.java)
                    val command =
                        object : VitalPluginCommand.Bungee(info.name) {
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
}
