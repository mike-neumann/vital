package me.vitalframework.statistics.command;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.Vital;
import me.vitalframework.VitalSubModule;
import me.vitalframework.annotation.RequiresBungeecord;
import me.vitalframework.annotation.RequiresSpigot;
import me.vitalframework.commands.VitalCommand;
import me.vitalframework.statistics.config.VitalStatisticsConfig;
import me.vitalframework.statistics.task.VitalHealthCheckTask;
import me.vitalframework.utils.VitalUtils;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.springframework.core.SpringVersion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Stats command to view vital's current runtime information
 */
public interface VitalStatsCommand<CS> {
    VitalHealthCheckTask getVitalHealthCheckTask();

    VitalStatisticsConfig getVitalStatisticsConfig();

    void sendMessage(CS sender, String message);

    default void handleOnBaseCommand(CS sender) {
        sendMessage(sender, "Spring version: <yellow>" + SpringVersion.getVersion());

        final String serverStatus;

        if (getVitalHealthCheckTask().getTps() >= getVitalStatisticsConfig().getMinTps()) {
            serverStatus = "<green>HEALTHY</green>";
        } else {
            serverStatus = "<red>UNHEALTHY</yellow>";
        }
        final var ramUsageInGigaBytes = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024 / 1024;
        final var vitalModuleNames = Vital.getContext().getBeansOfType(VitalSubModule.class);

        sendMessage(sender, "Server status: <yellow>" + getVitalHealthCheckTask().getTps() + " TPS (" + serverStatus + ")");
        sendMessage(sender, "RAM usage: <yellow>" + ramUsageInGigaBytes + " GB");
        sendMessage(sender, "Vital modules: <yellow>" + vitalModuleNames.size());

        for (var entry : vitalModuleNames.entrySet()) {
            sendMessage(sender, " - <yellow>" + entry.getKey());
        }
    }

    default VitalCommand.ReturnState handleOnHealthTps(CS sender) {
        sendMessage(sender, "TPS: <yellow>" + getVitalHealthCheckTask().getTps());
        sendMessage(sender, "TPS reports: <yellow>" + getVitalHealthCheckTask().getLastTps().size() + " of " + getVitalStatisticsConfig().getMaxTpsTaskCache());

        for (var entry : getVitalHealthCheckTask().getLastTps().entrySet()) {
            sendMessage(sender, " - <yellow>" + new SimpleDateFormat("HH:mm:ss").format(new Date(entry.getKey())) + ", " + entry.getValue() + " TPS");
        }

        sendMessage(sender, "Bad TPS reports: <yellow>" + getVitalHealthCheckTask().getLastUnhealthyTps().size() + " of " + getVitalStatisticsConfig().getMaxTpsTaskCache());

        for (var entry : getVitalHealthCheckTask().getLastUnhealthyTps().entrySet()) {
            sendMessage(sender, " - <yellow>" + new SimpleDateFormat("HH:mm:ss").format(new Date(entry.getKey())) + ", " + entry.getValue() + " TPS");
        }

        return VitalCommand.ReturnState.SUCCESS;
    }

    @Getter
    @RequiresSpigot
    class Spigot extends VitalCommand.Spigot implements VitalStatsCommand<CommandSender> {
        private final VitalHealthCheckTask vitalHealthCheckTask;
        private final VitalStatisticsConfig vitalStatisticsConfig;

        protected Spigot(VitalHealthCheckTask vitalHealthCheckTask, VitalStatisticsConfig vitalStatisticsConfig) {
            this.vitalHealthCheckTask = vitalHealthCheckTask;
            this.vitalStatisticsConfig = vitalStatisticsConfig;
        }

        @Override
        public void sendMessage(CommandSender sender, String message) {
            VitalUtils.spigot().sendMessage(sender, message);
        }

        @Override
        protected @NonNull VitalCommand.ReturnState onBaseCommand(@NonNull CommandSender sender) {
            VitalUtils.spigot().sendMessage(sender, "MC Version: <yellow>" + Bukkit.getVersion());
            VitalUtils.spigot().sendMessage(sender, "Bukkit Version: <yellow>" + Bukkit.getBukkitVersion());
            handleOnBaseCommand(sender);

            return VitalCommand.ReturnState.SUCCESS;
        }

        @VitalCommand.ArgHandler(@VitalCommand.Arg("tps"))
        public VitalCommand.ReturnState onTps(CommandSender sender) {
            return handleOnHealthTps(sender);
        }
    }

    @Getter
    @RequiresBungeecord
    class Bungeecord extends VitalCommand.Bungeecord implements VitalStatsCommand<net.md_5.bungee.api.CommandSender> {
        private final VitalHealthCheckTask vitalHealthCheckTask;
        private final VitalStatisticsConfig vitalStatisticsConfig;

        protected Bungeecord(VitalHealthCheckTask vitalHealthCheckTask, VitalStatisticsConfig vitalStatisticsConfig) {
            this.vitalHealthCheckTask = vitalHealthCheckTask;
            this.vitalStatisticsConfig = vitalStatisticsConfig;
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.CommandSender sender, String message) {
            VitalUtils.bungeecord().sendMessage(sender, message);
        }

        @Override
        protected @NonNull VitalCommand.ReturnState onBaseCommand(net.md_5.bungee.api.@NonNull CommandSender sender) {
            VitalUtils.bungeecord().sendMessage(sender, "Bungee version: <yellow>" + ProxyServer.getInstance().getVersion());
            handleOnBaseCommand(sender);

            return VitalCommand.ReturnState.SUCCESS;
        }

        @VitalCommand.ArgHandler(@VitalCommand.Arg("tps"))
        public VitalCommand.ReturnState onTps(net.md_5.bungee.api.CommandSender sender) {
            return handleOnHealthTps(sender);
        }
    }
}