package me.xra1ny.vital.cloudnet4.bridge;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.BridgeDocProperties;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.ServicePlayer;
import eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor;
import lombok.NonNull;
import me.xra1ny.vital.cloudnet4.driver.CloudNet4Driver;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public interface CloudNet4Bridge {
    @NonNull
    static ServiceRegistry getServiceRegistry() {
        return InjectionLayer.ext().instance(ServiceRegistry.class);
    }

    @NonNull
    static PlayerManager getPlayerManager() {
        return getServiceRegistry().firstProvider(PlayerManager.class);
    }

    @NonNull
    static PlayerExecutor getPlayerExecutor(@NonNull Player player) {
        return getPlayerManager().playerExecutor(player.getUniqueId());
    }

    static boolean isProxy(@NonNull ServiceInfoSnapshot serviceInfoSnapshot) {
        return switch(serviceInfoSnapshot.configuration().processConfig().environment()) {
            case "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true;
            default -> false;
        };
    }

    @NonNull
    static Optional<ServiceInfoSnapshot> getCloudServerByPlayer(@NonNull Player player) {
        return CloudNet4Driver.getCloudServers(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).stream()
                        .map(ServicePlayer::uniqueId)
                        .anyMatch(player.getUniqueId()::equals))
                .stream()
                .findFirst();
    }

    @NonNull
    static Optional<ServiceInfoSnapshot> getNonProxyCloudServerByPlayer(@NonNull Player player) {
        return CloudNet4Driver.getCloudServers(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).stream()
                        .map(ServicePlayer::uniqueId)
                        .anyMatch(player.getUniqueId()::equals) &&
                        !isProxy(server))
                .stream()
                .findFirst();
    }

    static void connect(@NonNull Player player, @NonNull String serverName) {
        getPlayerExecutor(player).connect(serverName);
    }

    static int getPlayerCount(@NonNull String taskName) {
        return CloudNet4Driver.getCloudServers(taskName).stream()
                .map(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).size())
                .reduce(0, Integer::sum);
    }
}