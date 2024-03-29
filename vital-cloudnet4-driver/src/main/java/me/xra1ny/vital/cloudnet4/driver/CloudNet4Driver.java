package me.xra1ny.vital.cloudnet4.driver;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceFactory;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.provider.ServiceTaskProvider;
import eu.cloudnetservice.driver.service.ServiceConfiguration;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.driver.service.ServiceTask;
import lombok.NonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

public interface CloudNet4Driver {
    @NonNull
    static CloudServiceProvider getCloudServiceProvider() {
        return InjectionLayer.ext().instance(CloudServiceProvider.class);
    }

    @NonNull
    static ServiceTaskProvider getServiceTaskProvider() {
        return InjectionLayer.ext().instance(ServiceTaskProvider.class);
    }

    @NonNull
    static CloudServiceFactory getCloudServiceFactory() {
        return InjectionLayer.ext().instance(CloudServiceFactory.class);
    }

    @NonNull
    static List<ServiceInfoSnapshot> getCloudServers(@NonNull Predicate<ServiceInfoSnapshot> predicate) {
        return getCloudServiceProvider().runningServices().stream()
                .filter(predicate)
                .toList();
    }

    @NonNull
    static List<ServiceInfoSnapshot> getCloudServers() {
        return getCloudServers(t -> true);
    }

    @NonNull
    static List<ServiceInfoSnapshot> getCloudServers(@NonNull String taskName) {
        return getCloudServiceProvider().servicesByTask(taskName).stream()
                .toList();
    }

    /**
     * Gets the cloud server by the given name; or an empty optional instance.
     *
     * @param serverName The name of the server, e.g: Lobby-1
     * @return An Optional holding either the value of the fetched cloud server; or empty if not found.
     */
    @NonNull
    static Optional<ServiceInfoSnapshot> getCloudServer(@NonNull String serverName) {
        return Optional.ofNullable(getCloudServiceProvider().serviceByName(serverName));
    }

    /**
     * Gets the task of the given server task name.
     *
     * @param taskName The task name.
     * @return An Optional holding either the server task; or empty.
     */
    @NonNull
    static Optional<ServiceTask> getServerTask(@NonNull String taskName) {
        return Optional.ofNullable(getServiceTaskProvider().serviceTask(taskName));
    }

    /**
     * Attempts to start a server with the given name, e.g: Lobby-1.
     * This method with attempt to grab the task of the given server name and use its configuration.
     *
     * @param taskName The name of the task to pull the configuration from.
     * @throws NoSuchElementException When the server task could not be located.
     */
    static void startCloudServer(@NonNull String taskName) throws NoSuchElementException {
        final ServiceTask serviceTask = getServerTask(taskName)
                .orElseThrow();

        getCloudServiceFactory().createCloudService(ServiceConfiguration.builder(serviceTask)
                .build());
    }

    /**
     * Attempts to stop a cloud server with the given name, e.g: Lobby-1.
     *
     * @param serverName The server name.
     */
    static void stopCloudServer(@NonNull String serverName) {
        final ServiceInfoSnapshot serviceInfoSnapshot = getCloudServer(serverName)
                .orElseThrow();

        serviceInfoSnapshot.provider().delete();
    }
}