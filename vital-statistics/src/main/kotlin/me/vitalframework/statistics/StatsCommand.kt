package me.vitalframework.statistics

import me.vitalframework.*
import me.vitalframework.commands.VitalCommand
import me.vitalframework.utils.VitalUtils.Bungee.sendFormattedMessage
import me.vitalframework.utils.VitalUtils.Spigot.sendFormattedMessage
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.springframework.core.SpringVersion
import java.text.SimpleDateFormat
import java.util.*

interface StatsCommand<CS> {
    val statisticsService: VitalStatisticsService
    val statisticsConfig: VitalStatisticsConfig

    fun sendMessage(sender: CS, message: String)

    fun handleOnCommand(sender: CS) {
        val serverStatus = if (statisticsService.tps >= statisticsConfig.minTps) "<green>HEALTHY</green>" else "<red>UNHEALTHY</yellow>"
        val ramUsageInGigaBytes = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024 / 1024
        val vitalModuleNames = Vital.CONTEXT.getBeansOfType(VitalSubModule::class.java)

        sendMessage(sender, "Spring version: <yellow>${SpringVersion.getVersion()}")
        sendMessage(sender, "Server status: <yellow>${statisticsService.tps} TPS ($serverStatus)")
        sendMessage(sender, "RAM usage: <yellow>$ramUsageInGigaBytes GB")
        sendMessage(sender, "Vital modules: <yellow>${vitalModuleNames.size}")

        for ((name, _) in vitalModuleNames) {
            sendMessage(sender, " - <yellow>$name")
        }
    }

    fun handleOnHealthTps(sender: CS): VitalCommand.ReturnState {
        sendMessage(sender, "TPS: <yellow>${statisticsService.tps}")
        sendMessage(
            sender,
            "TPS reports: <yellow>${statisticsService.lastTps.size} of ${statisticsConfig.maxTpsTaskCache}"
        )

        for ((time, tps) in statisticsService.lastTps) {
            sendMessage(sender, " - <yellow>${SimpleDateFormat("HH:mm:ss").format(Date(time))}, $tps TPS")
        }

        sendMessage(
            sender,
            "Bad TPS reports: <yellow>${statisticsService.lastUnhealthyTps.size} of ${statisticsConfig.maxTpsTaskCache}"
        )

        for ((time, tps) in statisticsService.lastUnhealthyTps) {
            sendMessage(sender, " - <yellow>${SimpleDateFormat("HH:mm:ss").format(Date(time))}, $tps TPS")
        }

        return VitalCommand.ReturnState.SUCCESS
    }

    @RequiresSpigot
    class Spigot(
        plugin: SpigotPlugin,
        override val statisticsService: VitalStatisticsService,
        override val statisticsConfig: VitalStatisticsConfig,
    ) : VitalCommand.Spigot(plugin), StatsCommand<SpigotCommandSender> {
        override fun sendMessage(sender: SpigotCommandSender, message: String) = sender.sendFormattedMessage(message)

        override fun onBaseCommand(sender: SpigotCommandSender): ReturnState {
            sender.sendFormattedMessage("MC Version: <yellow>${Bukkit.getVersion()}")
            sender.sendFormattedMessage("Bukkit Version: <yellow>${Bukkit.getBukkitVersion()}")
            handleOnCommand(sender)

            return ReturnState.SUCCESS
        }

        @ArgHandler(arg = Arg("tps"))
        fun onTps(sender: SpigotCommandSender) = handleOnHealthTps(sender)
    }

    @RequiresBungee
    class Bungee(
        plugin: BungeePlugin,
        override val statisticsService: VitalStatisticsService,
        override val statisticsConfig: VitalStatisticsConfig,
    ) : VitalCommand.Bungee(plugin), StatsCommand<BungeeCommandSender> {
        override fun sendMessage(sender: BungeeCommandSender, message: String) = sender.sendFormattedMessage(message)

        override fun onBaseCommand(sender: BungeeCommandSender): ReturnState {
            sender.sendFormattedMessage("Bungee version: <yellow>${ProxyServer.getInstance().version}")
            handleOnCommand(sender)

            return ReturnState.SUCCESS
        }

        @ArgHandler(arg = Arg("tps"))
        fun onTps(sender: BungeeCommandSender) = handleOnHealthTps(sender)
    }
}