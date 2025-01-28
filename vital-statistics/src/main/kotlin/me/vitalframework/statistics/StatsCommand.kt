package me.vitalframework.statistics

import me.vitalframework.*
import me.vitalframework.commands.VitalCommand
import me.vitalframework.statistics.config.VitalStatisticsConfig
import me.vitalframework.utils.VitalUtils
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
        val serverStatus =
            when (statisticsService.tps >= statisticsConfig.minTps) {
                true -> "<green>HEALTHY</green>"
                false -> "<red>UNHEALTHY</yellow>"
            }

        val ramUsageInGigaBytes =
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024 / 1024
        val vitalModuleNames = Vital.context.getBeansOfType(VitalSubModule::class.java)

        sendMessage(sender, "Spring version: <yellow>${SpringVersion.getVersion()}")
        sendMessage(sender, "Server status: <yellow>${statisticsService.tps} TPS ($serverStatus)")
        sendMessage(sender, "RAM usage: <yellow>$ramUsageInGigaBytes GB")
        sendMessage(sender, "Vital modules: <yellow>${vitalModuleNames.size}")

        vitalModuleNames.forEach { (name, module) ->
            sendMessage(sender, " - <yellow>$name")
        }
    }

    fun handleOnHealthTps(sender: CS): VitalCommand.ReturnState {
        sendMessage(sender, "TPS: <yellow>${statisticsService.tps}")
        sendMessage(
            sender,
            "TPS reports: <yellow>${statisticsService.lastTps.size} of ${statisticsConfig.maxTpsTaskCache}"
        )

        statisticsService.lastTps.forEach { (time, tps) ->
            sendMessage(sender, " - <yellow>${SimpleDateFormat("HH:mm:ss").format(Date(time))}, $tps TPS")
        }

        sendMessage(
            sender,
            "Bad TPS reports: <yellow>${statisticsService.lastUnhealthyTps.size} of ${statisticsConfig.maxTpsTaskCache}"
        )

        statisticsService.lastUnhealthyTps.forEach { (time, tps) ->
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
        override fun sendMessage(sender: SpigotCommandSender, message: String) {
            VitalUtils.Spigot.sendMessage(sender, message)
        }

        override fun onBaseCommand(sender: SpigotCommandSender): ReturnState {
            VitalUtils.Spigot.sendMessage(sender, "MC Version: <yellow>${Bukkit.getVersion()}")
            VitalUtils.Spigot.sendMessage(sender, "Bukkit Version: <yellow>${Bukkit.getBukkitVersion()}")
            handleOnCommand(sender)

            return ReturnState.SUCCESS
        }

        @ArgHandler(Arg("tps"))
        fun onTps(sender: SpigotCommandSender): ReturnState {
            return handleOnHealthTps(sender)
        }
    }

    @RequiresBungee
    class Bungee(
        plugin: BungeePlugin,
        override val statisticsService: VitalStatisticsService,
        override val statisticsConfig: VitalStatisticsConfig,
    ) : VitalCommand.Bungee(plugin), StatsCommand<BungeeCommandSender> {
        override fun sendMessage(sender: BungeeCommandSender, message: String) {
            VitalUtils.Bungee.sendMessage(sender, message)
        }

        override fun onBaseCommand(sender: BungeeCommandSender): ReturnState {
            VitalUtils.Bungee.sendMessage(sender, "Bungee version: <yellow>${ProxyServer.getInstance().version}")
            handleOnCommand(sender)

            return ReturnState.SUCCESS
        }

        @ArgHandler(Arg("tps"))
        fun onTps(sender: BungeeCommandSender): ReturnState {
            return handleOnHealthTps(sender)
        }
    }
}