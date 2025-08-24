package me.vitalframework.statistics

import me.vitalframework.BungeeCommandSender
import me.vitalframework.BungeePlugin
import me.vitalframework.RequiresBungee
import me.vitalframework.RequiresSpigot
import me.vitalframework.SpigotCommandSender
import me.vitalframework.SpigotPlugin
import me.vitalframework.Vital
import me.vitalframework.commands.VitalCommand
import me.vitalframework.utils.VitalUtils.Bungee.sendFormattedMessage
import me.vitalframework.utils.VitalUtils.Spigot.sendFormattedMessage
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit
import org.springframework.core.SpringVersion
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.Date

interface StatsCommand<CS> {
    val statisticsService: VitalStatisticsService
    val statisticsConfig: VitalStatisticsConfig

    fun sendMessage(
        sender: CS,
        message: String,
    )

    fun handleOnCommand(sender: CS) {
        val serverStatus = if (statisticsService.tps >= statisticsConfig.minTps) "<green>HEALTHY</green>" else "<red>UNHEALTHY</yellow>"
        val ramUsageInGigaBytes = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 / 1024

        sendMessage(sender, "-----> Server-Statistics")
        sendMessage(sender, "Spring version: <yellow>${SpringVersion.getVersion()}")
        sendMessage(sender, "Server status: <yellow>${statisticsService.tps} TPS ($serverStatus)")
        sendMessage(sender, "RAM usage: <yellow>$ramUsageInGigaBytes GB")
        sendMessage(sender, "Vital sub-modules: <yellow>${Vital.vitalSubModules.size}")

        for (name in Vital.vitalSubModules) {
            sendMessage(sender, "> <yellow>$name")
        }

        sendMessage(sender, "-----")
    }

    fun handleOnHealthTps(sender: CS): VitalCommand.ReturnState {
        sendMessage(sender, "-----> TPS")
        sendMessage(sender, "TPS: <yellow>${statisticsService.tps}")
        sendMessage(
            sender,
            "TPS reports: <yellow>${statisticsService.lastTps.size} of ${statisticsConfig.maxTpsTaskCache}",
        )

        for ((time, tps) in statisticsService.lastTps) {
            sendMessage(sender, "> <yellow>${SimpleDateFormat("HH:mm:ss").format(Date(time))}, $tps TPS")
        }

        sendMessage(
            sender,
            "Bad TPS reports: <yellow>${statisticsService.lastUnhealthyTps.size} of ${statisticsConfig.maxTpsTaskCache}",
        )

        for ((time, tps) in statisticsService.lastUnhealthyTps) {
            sendMessage(sender, "> <yellow>${SimpleDateFormat("HH:mm:ss").format(Date(time))}, $tps TPS")
        }

        sendMessage(sender, "-----")

        return VitalCommand.ReturnState.SUCCESS
    }

    @RequiresSpigot
    @Component
    class Spigot(
        plugin: SpigotPlugin,
        override val statisticsService: VitalStatisticsService,
        override val statisticsConfig: VitalStatisticsConfig,
    ) : VitalCommand.Spigot(plugin),
        StatsCommand<SpigotCommandSender> {
        override fun sendMessage(
            sender: SpigotCommandSender,
            message: String,
        ) = sender.sendFormattedMessage(message)

        @ArgHandler(Arg())
        fun onNoArg(sender: SpigotCommandSender): ReturnState {
            sender.sendFormattedMessage("MC Version: <yellow>${Bukkit.getVersion()}")
            sender.sendFormattedMessage("Bukkit Version: <yellow>${Bukkit.getBukkitVersion()}")
            handleOnCommand(sender)

            return ReturnState.SUCCESS
        }

        @ArgHandler(arg = Arg("tps"))
        fun onTps(sender: SpigotCommandSender) = handleOnHealthTps(sender)
    }

    @RequiresBungee
    @Component
    class Bungee(
        plugin: BungeePlugin,
        override val statisticsService: VitalStatisticsService,
        override val statisticsConfig: VitalStatisticsConfig,
    ) : VitalCommand.Bungee(plugin),
        StatsCommand<BungeeCommandSender> {
        override fun sendMessage(
            sender: BungeeCommandSender,
            message: String,
        ) = sender.sendFormattedMessage(message)

        @ArgHandler(Arg())
        fun onNoArg(sender: BungeeCommandSender): ReturnState {
            sender.sendFormattedMessage("Bungee version: <yellow>${ProxyServer.getInstance().version}")
            handleOnCommand(sender)

            return ReturnState.SUCCESS
        }

        @ArgHandler(arg = Arg("tps"))
        fun onTps(sender: BungeeCommandSender) = handleOnHealthTps(sender)
    }
}
