package me.xra1ny.vital.cloudnet4.driver;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceFactory;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.service.ServiceConfiguration;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.driver.service.ServiceTask;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class to interact with the cloudnet v4 driver module more easily.
 *
 * @author xRa1ny
 */
public class CloudNet4Driver {
    @NonNull
    public static CloudServiceProvider getCloudServiceProvider() {
        return InjectionLayer.ext().instance(CloudServiceProvider.class);
    }

    @NonNull
    public static ServiceTaskProvider getServiceTaskProvider() {
        return InjectionLayer.ext().instance(ServiceTaskProvider.class);
    }

    @NonNull
    public static CloudServiceFactory getCloudServiceFactory() {
        return InjectionLayer.ext().instance(CloudServiceFactory.class);
    }

    @NonNull
    public static List<ServiceInfoSnapshot> getCloudServers(@NonNull Predicate<ServiceInfoSnapshot> predicate) {
        return getCloudServiceProvider().runningServices().stream()
                .filter(predicate)
                .toList();
    }

    @NonNull
    public static List<ServiceInfoSnapshot> getCloudServers() {
        return getCloudServers(t -> true);
    }

    @NonNull
    public static List<ServiceInfoSnapshot> getCloudServers(@NonNull String taskName) {
        return getCloudServiceProvider().servicesByTask(taskName).stream()
                .toList();
    }

    /**
     * Gets the cloud server by the given name; or null;
     *
     * @param serverName The name of the server, e.g: Lobby-1
     * @return The fetched cloud server; or null.
     */
    @Nullable
    public static ServiceInfoSnapshot getCloudServer(@NonNull String serverName) {
        return getCloudServiceProvider().serviceByName(serverName);
    }

    /**
     * Gets the task of the given server task name.
     *
     * @param taskName The task name.
     * @return The fetched cloud server; or null.
     */
    @Nullable
    public static ServiceTask getServerTask(@NonNull String taskName) {
        return getServiceTaskProvider().serviceTask(taskName);
    }

    /**
     * Attempts to start a server with the given name, e.g: Lobby-1.
     * This method with attempt to grab the task of the given server name and use its configuration.
     *
     * @param taskName The name of the task to pull the configuration from.
     * @throws NoSuchElementException When the server task could not be located.
     */
    public static void startCloudServer(@NonNull String taskName) throws NoSuchElementException {
        final ServiceTask serviceTask = Optional.ofNullable(getServerTask(taskName))
                .orElseThrow();

        getCloudServiceFactory().createCloudService(ServiceConfiguration.builder(serviceTask)
                .build());
    }

    /**
     * Attempts to stop a cloud server with the given name, e.g: Lobby-1.
     *
     * @param serverName The server name.
     */
    public static void stopCloudServer(@NonNull String serverName) {
        final ServiceInfoSnapshot serviceInfoSnapshot = Optional.ofNullable(getCloudServer(serverName))
                .orElseThrow();

        serviceInfoSnapshot.provider().delete();
    }
}