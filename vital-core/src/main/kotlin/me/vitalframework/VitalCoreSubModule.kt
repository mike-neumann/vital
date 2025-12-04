package me.vitalframework

import org.bukkit.Bukkit
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

typealias SpigotEventHandler = org.bukkit.event.EventHandler
typealias BungeeEventHandler = net.md_5.bungee.event.EventHandler
typealias SpigotCommandSender = org.bukkit.command.CommandSender
typealias BungeeCommandSender = net.md_5.bungee.api.CommandSender
typealias SpigotPlugin = org.bukkit.plugin.java.JavaPlugin
typealias BungeePlugin = net.md_5.bungee.api.plugin.Plugin
typealias SpigotListener = org.bukkit.event.Listener
typealias BungeeListener = net.md_5.bungee.api.plugin.Listener
typealias SpigotPlayer = org.bukkit.entity.Player
typealias BungeePlayer = net.md_5.bungee.api.connection.ProxiedPlayer
typealias SpigotEventPriority = org.bukkit.event.EventPriority
typealias BungeeEventPriority = net.md_5.bungee.event.EventPriority
typealias SpigotRunnable = org.bukkit.scheduler.BukkitRunnable
typealias BungeeRunnable = Runnable
typealias SpigotTask = org.bukkit.scheduler.BukkitTask
typealias BungeeTask = net.md_5.bungee.api.scheduler.ScheduledTask
typealias SpigotEvent = org.bukkit.event.Event
typealias BungeeEvent = net.md_5.bungee.api.plugin.Event
typealias SpigotCancellable = org.bukkit.event.Cancellable
typealias BungeeCancellable = net.md_5.bungee.api.plugin.Cancellable
typealias SpigotBStatsMetrics = org.bstats.bukkit.Metrics
typealias BungeeBStatsMetrics = org.bstats.bungeecord.Metrics

@Component
class VitalCoreSubModule {
    val logger = logger()

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(e: ApplicationReadyEvent) {
        logger.info("Vital up and running in '${"%.3f seconds".format(e.timeTaken.toMillis() / 1000.0)}'")
        logger.info("Thanks for using Vital!")
    }

    @RequiresSpigot
    @Component("vital-core")
    class Spigot(
        val plugin: SpigotPlugin,
        val vitalListeners: List<VitalListener.Spigot>,
    ) : VitalSubModule() {
        val logger = logger()

        override fun onInstall() {
            for (vitalListener in vitalListeners) {
                try {
                    Bukkit.getPluginManager().registerEvents(vitalListener, plugin)
                    logger.info("Spigot listener '${vitalListener::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    logger.error("Error while registering spigot listener '${vitalListener::class.java.name}'", e)
                }
            }
        }
    }

    @RequiresBungee
    @Component("vital-core")
    class Bungee(
        val plugin: BungeePlugin,
        val vitalListeners: List<VitalListener.Bungee>,
    ) : VitalSubModule() {
        val logger = logger()

        override fun onInstall() {
            for (vitalListener in vitalListeners) {
                try {
                    plugin.proxy.pluginManager.registerListener(plugin, vitalListener)
                    logger.info("Bungee listener '${vitalListener::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    logger.error("Error while registering bungee listener '${vitalListener::class.java.name}'", e)
                }
            }
        }
    }

    companion object {
        /**
         * Extension function to provide a logger for the calling class.
         *
         * This function leverages the class's runtime type to initialize a logger instance
         * using SLF4J's `LoggerFactory`.
         *
         * @receiver The instance of the class for which the logger is being created.
         * @return A logger instance associated with the calling class.
         */
        @JvmStatic
        fun <T : Any> T.logger() = LoggerFactory.getLogger(this::class.java)!!

        /**
         * Retrieves the required annotation of a specified type from the current class.
         *
         * This function checks whether the current class is annotated with the specified annotation.
         * If the annotation is not present, an exception is thrown.
         *
         * @param T The type of annotation to retrieve. Must extend [Annotation].
         * @return The annotation of type [T] if it exists on the class.
         * @throws RuntimeException If the class is not annotated with the specified annotation type.
         */
        @JvmStatic
        inline fun <reified T : Annotation> Class<*>.getRequiredAnnotation() =
            getAnnotation(T::class.java)
                ?: throw RuntimeException("$simpleName must be annotated with '@${T::class.java.name}'")

        /**
         * Retrieves the required annotation of a specified type from the current class.
         *
         * This function checks whether the current class is annotated with the specified annotation.
         * If the annotation is not present, an exception is thrown.
         *
         * @param T The type of annotation to retrieve. Must extend [Annotation].
         * @return The annotation of type [T] if it exists on the class.
         * @throws RuntimeException If the class is not annotated with the specified annotation type.
         */
        @JvmStatic
        inline fun <reified T : Annotation> KClass<*>.getRequiredAnnotation() = java.getRequiredAnnotation<T>()

        /**
         * Retrieves all annotations of a specified type applied to the current class.
         * Throws a RuntimeException if no annotations of the specified type are found.
         *
         * @param T The type of annotations to retrieve. Must extend [Annotation].
         * @return A list of annotations of type [T] present on the current class.
         * @throws RuntimeException if no annotations of type [T] are found on the class.
         */
        @JvmStatic
        inline fun <reified T : Annotation> Class<*>.getRequiredAnnotations(): List<T> =
            javaClass.getAnnotationsByType(T::class.java).toList().also {
                if (it.isEmpty()) throw RuntimeException("${javaClass.simpleName} must be annotated with '@${T::class.java.name}'")
            }

        /**
         * Retrieves all annotations of a specified type applied to the current class.
         * Throws a RuntimeException if no annotations of the specified type are found.
         *
         * @param T The type of annotations to retrieve. Must extend [Annotation].
         * @return A list of annotations of type [T] present on the current class.
         * @throws RuntimeException if no annotations of type [T] are found on the class.
         */
        @JvmStatic
        inline fun <reified T : Annotation> KClass<*>.getRequiredAnnotations(): List<T> = java.getRequiredAnnotations<T>()

        /**
         * Retrieves the `Vital.Info` annotation from the current class.
         *
         * This method extracts the `Vital.Info` annotation, which contains metadata about a Vital plugin,
         * such as its name, description, version, and supported environment. It is expected that the class
         * invoking this method is annotated with `@Vital.Info`. If the annotation is not present, an exception will be thrown.
         *
         * @return The `Vital.Info` annotation associated with the class.
         * @throws RuntimeException If the class is not annotated with `@Vital.Info`.
         */
        @JvmStatic
        fun Class<*>.getVitalInfo() = getRequiredAnnotation<Vital.Info>()

        /**
         * Retrieves the `Vital.Info` annotation from the current class.
         *
         * This method extracts the `Vital.Info` annotation, which contains metadata about a Vital plugin,
         * such as its name, description, version, and supported environment. It is expected that the class
         * invoking this method is annotated with `@Vital.Info`. If the annotation is not present, an exception will be thrown.
         *
         * @return The `Vital.Info` annotation associated with the class.
         * @throws RuntimeException If the class is not annotated with `@Vital.Info`.
         */
        @JvmStatic
        fun KClass<*>.getVitalInfo() = java.getVitalInfo()
    }
}
