package me.xra1ny.vital.statistics.command;

import lombok.Getter;
import lombok.NonNull;
import me.xra1ny.vital.Vital;
import me.xra1ny.vital.VitalSubModule;
import me.xra1ny.vital.annotation.RequiresBungeecord;
import me.xra1ny.vital.annotation.RequiresSpigot;
import me.xra1ny.vital.commands.VitalCommand;
import me.xra1ny.vital.commands.VitalCommandReturnState;
import me.xra1ny.vital.commands.annotation.VitalCommandArg;
import me.xra1ny.vital.commands.annotation.VitalCommandArgHandler;
import me.xra1ny.vital.commands.annotation.VitalCommandInfo;
import me.xra1ny.vital.statistics.config.VitalStatisticsConfig;
import me.xra1ny.vital.statistics.task.HealthCheckTask;
import me.xra1ny.vital.utils.VitalUtils;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.springframework.core.SpringVersion;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Stats command to view vital's current runtime information
 */
public interface StatsCommand<CS> {
    HealthCheckTask getHealthCheckTask();

    VitalStatisticsConfig getVitalStatisticsConfig();

    void sendMessage(CS sender, String message);

    default void handleOnBaseCommand(CS sender) {
        sendMessage(sender, "Spring version: <yellow>" + SpringVersion.getVersion());

        final String serverStatus;

        if (getHealthCheckTask().getTps() >= getVitalStatisticsConfig().getMinTps()) {
            serverStatus = "<green>HEALTHY</green>";
        } else {
            serverStatus = "<red>UNHEALTHY</yellow>";
        }

        sendMessage(sender, "Server status: <yellow>" + getHealthCheckTask().getTps() + " TPS (" + serverStatus + ")");

        final double ramUsageInGigaBytes = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024f / 1024 / 1024;

        sendMessage(sender, "RAM usage: <yellow>" + ramUsageInGigaBytes + " GB");

        final Map<String, VitalSubModule> vitalModuleNames = Vital.getContext().getBeansOfType(VitalSubModule.class);

        sendMessage(sender, "Vital modules: <yellow>" + vitalModuleNames.size());

        for (Map.Entry<String, VitalSubModule> entry : vitalModuleNames.entrySet()) {
            sendMessage(sender, " - <yellow>" + entry.getKey());
        }
    }

    default VitalCommandReturnState handleOnHealthTps(CS sender) {
        sendMessage(sender, "TPS: <yellow>" + getHealthCheckTask().getTps());
        sendMessage(sender, "TPS reports: <yellow>" + getHealthCheckTask().getLastTps().size() + " of " + getVitalStatisticsConfig().getMaxTpsTaskCache());

        for (Map.Entry<Long, Integer> entry : getHealthCheckTask().getLastTps().entrySet()) {
            sendMessage(sender, " - <yellow>" + new SimpleDateFormat("HH:mm:ss").format(new Date(entry.getKey())) + ", " + entry.getValue() + " TPS");

        }

        sendMessage(sender, "Bad TPS reports: <yellow>" + getHealthCheckTask().getLastUnhealthyTps().size() + " of " + getVitalStatisticsConfig().getMaxTpsTaskCache());

        for (Map.Entry<Long, Integer> entry : getHealthCheckTask().getLastUnhealthyTps().entrySet()) {
            sendMessage(sender, " - <yellow>" + new SimpleDateFormat("HH:mm:ss").format(new Date(entry.getKey())) + ", " + entry.getValue() + " TPS");
        }

        return VitalCommandReturnState.SUCCESS;
    }

    @Getter
    @RequiresSpigot
    @VitalCommandInfo(
            name = "health",
            requiresPlayer = false,
            permission = "vital.command.health",
            args = {
                    @VitalCommandArg("tps")
            }
    )
    class Spigot extends VitalCommand.Spigot implements StatsCommand<CommandSender> {
        private final HealthCheckTask healthCheckTask;
        private final VitalStatisticsConfig vitalStatisticsConfig;

        protected Spigot(HealthCheckTask healthCheckTask, VitalStatisticsConfig vitalStatisticsConfig) {
            this.healthCheckTask = healthCheckTask;
            this.vitalStatisticsConfig = vitalStatisticsConfig;
        }

        @Override
        public void sendMessage(CommandSender sender, String message) {
            VitalUtils.spigot().sendMessage(sender, message);
        }

        @Override
        protected @NonNull VitalCommandReturnState onBaseCommand(@NonNull CommandSender sender) {
            VitalUtils.spigot().sendMessage(sender, "MC Version: <yellow>" + Bukkit.getVersion());
            VitalUtils.spigot().sendMessage(sender, "Bukkit Version: <yellow>" + Bukkit.getBukkitVersion());
            handleOnBaseCommand(sender);

            return VitalCommandReturnState.SUCCESS;
        }

        @VitalCommandArgHandler("tps")
        public VitalCommandReturnState onTps(CommandSender sender) {
            return handleOnHealthTps(sender);
        }
    }

    @Getter
    @RequiresBungeecord
    @VitalCommandInfo(
            name = "proxyhealth",
            requiresPlayer = false,
            permission = "vital.command.proxyHealth",
            args = {
                    @VitalCommandArg("tps")
            }
    )
    class Bungeecord extends VitalCommand.Bungeecord implements StatsCommand<net.md_5.bungee.api.CommandSender> {
        private final HealthCheckTask healthCheckTask;
        private final VitalStatisticsConfig vitalStatisticsConfig;

        protected Bungeecord(HealthCheckTask healthCheckTask, VitalStatisticsConfig vitalStatisticsConfig) {
            this.healthCheckTask = healthCheckTask;
            this.vitalStatisticsConfig = vitalStatisticsConfig;
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.CommandSender sender, String message) {
            VitalUtils.bungeecord().sendMessage(sender, message);
        }

        @Override
        protected @NonNull VitalCommandReturnState onBaseCommand(net.md_5.bungee.api.@NonNull CommandSender sender) {
            VitalUtils.bungeecord().sendMessage(sender, "Bungee version: <yellow>" + ProxyServer.getInstance().getVersion());
            handleOnBaseCommand(sender);

            return VitalCommandReturnState.SUCCESS;
        }

        @VitalCommandArgHandler("tps")
        public VitalCommandReturnState onTps(net.md_5.bungee.api.CommandSender sender) {
            return handleOnHealthTps(sender);
        }
    }
}
