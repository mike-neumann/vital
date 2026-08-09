package dev.vitalframework

import dev.vitalframework.VitalCoreModule.Companion.logger
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.SpringApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import java.util.Properties

/**
 * This class defines internal functionality required to initialize and shutdown a Vital plugin and should rarely be used by developers manually.
 * Plugin bootstrapping is automatically done via the `vital-gradle-plugin` and initialization is done by `vital-loader`.
 *
 * If you for some reason MUST initialize your Vital plugin manually, this would be the class you have to use.
 */
abstract class VitalPlugin(
    val pluginEnvironment: PluginEnvironment,
) {
    lateinit var context: ConfigurableApplicationContext
    val vitalModules = mutableListOf<String>()

    lateinit var metadata: Metadata
        private set

    /**
     * Returns if the given [vitalModuleName] is enabled.
     * Note: Modules are enabled sequentially, during the initialization phase,
     * this function might return false for a module that will be enabled later in the pipeline.
     */
    fun isVitalModuleEnabled(vitalModuleName: String): Boolean = vitalModules.contains(vitalModuleName)

    /**
     * Lifecycle function; called when this plugin is enabled.
     */
    open fun onEnable() {
        logger.debug("Lifecycle function 'onEnable' was not overridden by Vital plugin '$this'.")
    }

    /**
     * Shuts down the running Vital framework instance by killing the existing Spring context.
     */
    fun exit() {
        logger.info("Shutting down Vital.")

        if (context.isClosed) {
            logger.info("Vital is already being shut down.")
            return
        }

        logger.info("Disabling all modules.")

        // Disable all modules.
        val vitalModules = context.beanFactory.getBeansOfType<VitalModule>()
        logger.debug("Will disable '${vitalModules.size}' modules.")
        for ((_, vitalModule) in vitalModules) {
            val name = vitalModule.info[VitalModule.Info::class.java]?.value
            try {
                logger.debug("Disabling module '$name'.")
                vitalModule.disable(this)
            } catch (e: Exception) {
                SpringApplication.exit(context)
                logger.error(
                    "Vital was shut down, but an error occurred while disabling module '$name'.",
                    e,
                )
                return
            }
        }

        val exitCode = SpringApplication.exit(context)
        onDisable()
        logger.info("Vital exited with code '$exitCode'.")
    }

    /**
     * Lifecycle function; called when this plugin is disabled.
     */
    open fun onDisable() {
        logger.debug("Lifecycle function 'onDisable' was not overridden by Vital plugin '$this'.")
    }

    companion object {
        private val logger = logger()

        @JvmStatic
        lateinit var instance: VitalPlugin
            private set

        @JvmStatic
        val officialVitalModules =
            listOf(
                "vital-all",
                "vital-cloudnet4-bridge",
                "vital-cloudnet4-driver",
                "vital-commands",
                "vital-commands.spigot",
                "vital-commands.bungee",
                "vital-commands-processor",
                "vital-configs",
                "vital-core",
                "vital-core-processor",
                "vital-holograms",
                "vital-inventories",
                "vital-items",
                "vital-loader",
                "vital-localization",
                "vital-minigames",
                "vital-players",
                "vital-scoreboards",
                "vital-statistics",
                "vital-tasks",
                "vital-tests",
                "vital-utils",
            )

        /**
         * Returns if the given [vitalModuleName] is an officially known Vital module.
         * Every official Vital module starts with the prefix "vital-" and are collected in [officialVitalModules].
         */
        @JvmStatic
        fun isOfficialVitalModule(vitalModuleName: String): Boolean = officialVitalModules.contains(vitalModuleName)

        /**
         * Initializes the Vital framework by starting a lightweight Spring Boot application.
         * You should NEVER manually call this function.
         * Vital initialization should be done via `vital-loader`.
         * Calling this function manually in your plugin may cause classloader issues and unexpected errors during application initialization.
         *
         * IF you, for some reason, MUST use this function manually to initialize the framework, you should call this function on a separate Classloader, which is NOT the plugin classloader.
         * For examples, take a look at the sources for `vital-loader`.
         */
        @JvmStatic
        fun run(
            loader: Any,
            classLoader: ClassLoader,
        ) {
            logger.debug("Running Vital via plugin loader '{}' and class loader '{}'.", loader, classLoader)

            Thread.currentThread().contextClassLoader = classLoader
            ClassUtils.overrideThreadContextClassLoader(classLoader)

            logger.debug("Vital class loader successfully overridden to '{}'", classLoader)

            // load metadata from "vital-metadata.properties"
            logger.debug("Loading Vital metadata.")
            val metadata = loadMetadata(classLoader)
            logger.debug("Vital metadata successfully loaded.")

            logger.debug("Loading main class '${metadata.mainClassName}'.")
            val mainClass = Class.forName(metadata.mainClassName)
            logger.debug("Main class '${metadata.mainClassName}' successfully loaded")

            logger.debug("Loading internal Vital-generated plugin configuration class.")
            val pluginConfigurationClass = classLoader.loadClass("${mainClass.packageName}.PluginConfiguration")
            logger.debug("Internal Vital-generated plugin configuration class successfully loaded.")

            // start up spring boot using the previously generated "PluginConfiguration" class as the main class
            logger.debug("Running spring boot.")
            val context =
                SpringApplicationBuilder(pluginConfigurationClass)
                    // here we register the plugin instance as a bean so we can inject it elsewhere
                    .initializers({ it.beanFactory.registerSingleton("plugin", loader) })
                    // this is needed so spring can locate classes and resources that are on the plugin classpath
                    .resourceLoader(VitalResourceLoader())
                    .run()

            val vitalPlugins = context.getBeansWithAnnotation<Info>()
            if (vitalPlugins.isEmpty()) {
                throw VitalPluginException.NoMainPluginClass(metadata.mainClass, loader.javaClass)
            }

            if (vitalPlugins.size > 1) {
                throw VitalPluginException.MultipleMainPluginClasses(metadata.mainClass, loader.javaClass)
            }

            val vitalPlugin = vitalPlugins.values.first()
            if (vitalPlugin !is VitalPlugin) {
                throw VitalPluginException.InvalidMainPluginClassType(metadata.mainClass, loader.javaClass)
            }

            vitalPlugin.context = context
            instance = vitalPlugin
            logger.info("Vital up and running, enabling all modules.")

            // Once fully up and running, we can enable all modules.
            val vitalModules = context.beanFactory.getBeansOfType<VitalModule>()
            for ((_, vitalModule) in vitalModules) {
                try {
                    vitalModule.enable(vitalPlugin)
                } catch (e: Exception) {
                    logger.error("Error while enabling Vital module '${vitalModule.info[VitalModule.Info::class.java]?.value}'", e)
                    vitalPlugin.exit()
                    break
                }
            }

            vitalPlugin.onEnable()
        }

        /**
         * Loads the internal Vital metadata (vital.properties) from the classpath.
         * This file is used for internal configurations regarding some Vital internals.
         *
         * Developers should rarely use this file to configure anything.
         */
        private fun loadMetadata(classLoader: ClassLoader): Metadata {
            logger.debug("Loading Vital metadata.")

            val metadataProperties = Properties().apply { load(classLoader.getResourceAsStream(Metadata.FILE_NAME)) }
            val metadata = Metadata(metadataProperties[Metadata.Property.MAIN_CLASS].toString())

            logger.debug("Vital metadata loaded successfully, {}", metadata)
            return metadata
        }
    }

    data class Metadata(
        val mainClassName: String,
    ) {
        val mainClass: Class<*> get() = Class.forName(mainClassName)

        /**
         * Serializes this Metadata instance into a properties file string.
         */
        fun serialize() =
            """
            ${Property.MAIN_CLASS}=$mainClassName
            """.trimIndent()

        companion object {
            const val FILE_NAME = "vital-metadata.properties"
        }

        object Property {
            const val MAIN_CLASS = "vital.main-class"
        }
    }

    /**
     * Defines all plugin environments supported by Vital.
     */
    enum class PluginEnvironment(
        val ymlFileName: String,
    ) {
        SPIGOT("plugin.yml"),
        PAPER("plugin.yml"),
        BUNGEE("bungee.yml"),
    }

    /**
     * Defines the info for a Vital-powered plugin.
     * The data defined in this annotation will be used to generate the final plugin jar.
     * A plugin must always have exactly 1 main class that is annotated with this annotation.
     *
     * ```java
     * @VitalPlugin.Info(
     *   name = "name",
     *   description = "description",
     *   apiVersion = "1.21",
     *   version = "1.0.0",
     *   author = {"author1", "author2"}
     * )
     * public class MyPlugin extends VitalPlugin.Spigot {
     * }
     * ```
     */
    @Configuration(proxyBeanMethods = false)
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val description: String,
        val apiVersion: String,
        val version: String,
        val author: Array<String>,
        /**
         * This instructs Vital to scan additional packages for Vital functionality.
         * If you have to include an external dependency in your plugin that uses Vital,
         * like Commands, Configs, etc. this will be the place to define those packages.
         */
        val scanAdditionalPackages: Array<String> = [],
    ) {
        companion object {
            val DEFAULT_PACKAGES = arrayOf("dev.vitalframework")
        }
    }

    abstract class Spigot : VitalPlugin(PluginEnvironment.SPIGOT)

    abstract class Paper : VitalPlugin(PluginEnvironment.PAPER)

    abstract class Bungee : VitalPlugin(PluginEnvironment.BUNGEE)
}
