package me.xra1ny.vital.cloudnet4.bridge;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.BridgeDocProperties;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.ServicePlayer;
import eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import me.xra1ny.vital.cloudnet4.driver.CloudNet4Driver;

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

    /**
     * Gets the service registry.
     *
     * @return The service registry.
     */
    @NonNull
    public static ServiceRegistry getServiceRegistry() {
        return InjectionLayer.ext().instance(ServiceRegistry.class);
    }

    /**
     * Gets the player manager.
     *
     * @return The player manager.
     */
    @NonNull
    public static PlayerManager getPlayerManager() {
        return getServiceRegistry().firstProvider(PlayerManager.class);
    }

    /**
     * Gets the player executor of the given player uniqueId.
     *
     * @param uniqueId The player uniqueId.
     * @return The player executor of the given player uniqueId.
     */
    @NonNull
    public static PlayerExecutor getPlayerExecutor(@NonNull UUID uniqueId) {
        return getPlayerManager().playerExecutor(uniqueId);
    }

    /**
     * Checks if the given cloudnet service is a proxy.
     *
     * @param serviceInfoSnapshot The cloudnet service.
     * @return True if the service is a proxy; false otherwise.
     */
    public static boolean isProxy(@NonNull ServiceInfoSnapshot serviceInfoSnapshot) {
        return switch (serviceInfoSnapshot.configuration().processConfig().environment()) {
            case "JAVA_PROXY", "PE_PROXY", "BUNGEECORD", "VELOCITY", "WATERDOG_PE" -> true;
            default -> false;
        };
    }


    /**
     * Gets the cloudnet service the given player uniqueId is currently connected to.
     *
     * @param uniqueId The player uniqueId.
     * @return The cloudnet service; or null if not found.
     */
    @Nullable
    public static ServiceInfoSnapshot getCloudServerByPlayerUniqueId(@NonNull UUID uniqueId) {
        return CloudNet4Driver.getCloudServers(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).stream()
                        .map(ServicePlayer::uniqueId)
                        .anyMatch(uniqueId::equals))
                .stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the non-proxy cloudnet service the given player uniqueId is currently connected to.
     *
     * @param uniqueId The player uniqueId.
     * @return The cloudnet service; or null if not found.
     */
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

    /**
     * Connects the given player uniqueId with the given server name.
     *
     * @param uniqueId   The player uniqueId.
     * @param serverName The target server name.
     */
    public static void connect(@NonNull UUID uniqueId, @NonNull String serverName) {
        getPlayerExecutor(uniqueId).connect(serverName);
    }

    /**
     * Gets the player count of all cloudnet services by the given task name.
     *
     * @param taskName The cloudnet service task name.
     * @return The player count of all found services.
     */
    public static int getPlayerCount(@NonNull String taskName) {
        return CloudNet4Driver.getCloudServers(taskName).stream()
                .map(server -> server.readPropertyOrDefault(BridgeDocProperties.PLAYERS, List.of()).size())
                .reduce(0, Integer::sum);
    }

    /**
     * Runs the command as the given player
     *
     * @param uuid    The player uuid.
     * @param command The command to run.
     */
    public static void runCommand(@NonNull UUID uuid, @NonNull String command) {
        getPlayerExecutor(uuid).spoofCommandExecution(command);
    }
}