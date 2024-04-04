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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Utility class for easier interaction with the cloudnet v4 bridge module.
 *
 * @author xRa1ny
 */
public class CloudNet4Bridge {
    private CloudNet4Bridge() {
        // may not be instantiated.
    }

    @NonNull
    public static ServiceRegistry getServiceRegistry() {
        return InjectionLayer.ext().instance(ServiceRegistry.class);
    }

    @NonNull
    public static PlayerManager getPlayerManager() {
        return getServiceRegistry().firstProvider(PlayerManager.class);
    }

    @NonNull
    public static PlayerExecutor getPlayerExecutor(@NonNull UUID uniqueId) {
        return getPlayerManager().playerExecutor(uniqueId);
    }

    public static boolean isProxy(@NonNull ServiceInfoSnapshot serviceInfoSnapshot) {
        return switch(serviceInfoSnapshot.configuration().processConfig().environment()) {
            case "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true;
            default -> false;
        };
    }


    @Nullable
    public static ServiceInfoSnapshot getCloudServerByPlayerUniqueId(@NonNull UUID uniqueId) {
        return CloudNet4Driver.getCloudServers(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).stream()
                        .map(ServicePlayer::uniqueId)
                        .anyMatch(uniqueId::equals))
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static ServiceInfoSnapshot getNonProxyCloudServerByPlayerUniqueId(@NonNull UUID uniqueId) {
        return CloudNet4Driver.getCloudServers(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).stream()
                        .map(ServicePlayer::uniqueId)
                        .anyMatch(uniqueId::equals) &&
                        !isProxy(server))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static void connect(@NonNull UUID uniqueId, @NonNull String serverName) {
        getPlayerExecutor(uniqueId).connect(serverName);
    }

    public static int getPlayerCount(@NonNull String taskName) {
        return CloudNet4Driver.getCloudServers(taskName).stream()
                .map(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).size())
                .reduce(0, Integer::sum);
    }
}