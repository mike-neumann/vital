package dev.vitalframework

import org.bukkit.Bukkit
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Lazy
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
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

class VitalCoreModule {
    val logger = logger()

    @Order(Ordered.LOWEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(e: ApplicationReadyEvent) {
        logger.info("Vital up and running in '${"%.3f seconds".format(e.timeTaken.toMillis() / 1000.0)}'")
        logger.info("Thanks for using Vital!")
    }

    @Conditional(RequiresSpigot::class)
    @VitalModule.Info(value = "vital-core")
    class Spigot(
        val plugin: SpigotPlugin,
        @param:Lazy
        val vitalListeners: List<VitalListener.Spigot>,
    ) : VitalModule() {
        val logger = logger()

        override fun onEnable() {
            for (vitalListener in vitalListeners) {
                try {
                    Bukkit.getPluginManager().registerEvents(vitalListener, plugin)
                    logger.info("Spigot listener '${vitalListener::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    throw VitalListenerException.Register(vitalListener::class.java, e)
                }
            }
        }

        @ConditionalOnMissingBean
        @Bean
        fun vitalBStatsInitialization(
            plugin: SpigotPlugin,
            vitalCoreConfigurationProperties: VitalCoreConfigurationProperties,
        ): VitalBStatsInitialization.Spigot = VitalBStatsInitialization.Spigot(plugin, vitalCoreConfigurationProperties)

        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        @ConditionalOnMissingBean
        @Bean
        fun vitalShutdownHandler(vitalPlugin: VitalPlugin): VitalShutdownHandler.Spigot = VitalShutdownHandler.Spigot(vitalPlugin)
    }

    @Conditional(RequiresBungee::class)
    @VitalModule.Info(value = "vital-core")
    class Bungee(
        val plugin: BungeePlugin,
        @param:Lazy
        val vitalListeners: List<VitalListener.Bungee>,
    ) : VitalModule() {
        val logger = logger()

        override fun onEnable() {
            for (vitalListener in vitalListeners) {
                try {
                    plugin.proxy.pluginManager.registerListener(plugin, vitalListener)
                    logger.info("Bungee listener '${vitalListener::class.java.name}' successfully registered")
                } catch (e: Exception) {
                    throw VitalListenerException.Register(vitalListener::class.java, e)
                }
            }
        }

        @ConditionalOnMissingBean
        @Bean
        fun vitalBStatsInitialization(
            plugin: BungeePlugin,
            vitalCoreConfigurationProperties: VitalCoreConfigurationProperties,
        ): VitalBStatsInitialization.Bungee = VitalBStatsInitialization.Bungee(plugin, vitalCoreConfigurationProperties)

        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        @ConditionalOnMissingBean
        @Bean
        fun vitalShutdownHandler(vitalPlugin: VitalPlugin): VitalShutdownHandler.Bungee = VitalShutdownHandler.Bungee(vitalPlugin)
    }

    companion object {
        /**
         * Gets a [org.slf4j.Logger] for the given class.
         */
        @JvmStatic
        fun <T : Any> T.logger() = LoggerFactory.getLogger(this::class.java)!!

        /**
         * Gets the specified [annotation] from the given class.
         * If the annotation is not present, a [RuntimeException] is thrown.
         */
        @JvmStatic
        fun <T : Annotation> Class<*>.getRequiredAnnotation(annotation: Class<T>): T =
            AnnotationUtils.findAnnotation(this, annotation)
                ?: throw RuntimeException("$simpleName must be annotated with '@${annotation.name}'")

        /**
         * Gets the specified annotation from the given class.
         * If the annotation is not present, a [RuntimeException] is thrown.
         */
        @JvmStatic
        inline fun <reified T : Annotation> Class<*>.getRequiredAnnotation() = getRequiredAnnotation(T::class.java)

        /**
         * Gets the specified annotation from the given class.
         * If the annotation is not present, a [RuntimeException] is thrown.
         */
        @JvmStatic
        inline fun <reified T : Annotation> KClass<*>.getRequiredAnnotation() = java.getRequiredAnnotation<T>()

        /**
         * Gets all annotations of the specified [annotation] for the given class.
         * If no annotations were found, throws a [RuntimeException].
         */
        @JvmStatic
        fun <T : Annotation> Class<*>.getRequiredAnnotations(annotation: Class<T>): List<T> =
            javaClass.getAnnotationsByType(annotation).toList().also {
                if (it.isEmpty()) throw RuntimeException("${javaClass.simpleName} must be annotated with '@${annotation.name}'")
            }

        /**
         * Gets all annotations of the specified annotation for the given class.
         * If no annotations were found, throws a [RuntimeException].
         */
        @JvmStatic
        inline fun <reified T : Annotation> Class<*>.getRequiredAnnotations() = getRequiredAnnotations(T::class.java)

        /**
         * Gets all annotations of the specified annotation for the given class.
         * If no annotations were found, throws a [RuntimeException].
         */
        @JvmStatic
        inline fun <reified T : Annotation> KClass<*>.getRequiredAnnotations(): List<T> = java.getRequiredAnnotations<T>()
    }
}
