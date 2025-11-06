package me.vitalframework

import me.vitalframework.VitalCoreSubModule.Companion.logger
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils
import java.util.Properties

object Vital {
    private val log = logger()

    @JvmStatic
    lateinit var context: ConfigurableApplicationContext
        private set

    @JvmStatic
    val vitalSubModules = mutableListOf<String>()

    @JvmStatic
    lateinit var metadata: Metadata
        private set

    @JvmStatic
    fun run(
        loader: Any,
        classLoader: ClassLoader,
    ) {
        log.debug("Running Vital via plugin loader '{}' and class loader '{}'...", loader, classLoader)

        Thread.currentThread().contextClassLoader = classLoader
        ClassUtils.overrideThreadContextClassLoader(classLoader)

        log.debug("Vital class loader successfully overridden to '{}'", classLoader)

        // load metadata from "vital-metadata.properties"
        loadMetadata(classLoader)

        log.debug("Loading main class '${metadata.mainClassName}'...")
        val mainClass = Class.forName(metadata.mainClassName)
        log.debug("Main class '${metadata.mainClassName}' successfully loaded")

        // start up spring boot using the previously generated "PluginConfiguration" class as the main class
        log.debug("Running spring boot...")
        context =
            SpringApplicationBuilder(classLoader.loadClass("${mainClass.packageName}.PluginConfiguration"))
                // here we register the plugin instance as a bean so we can inject it elsewhere
                .initializers({ it.beanFactory.registerSingleton("plugin", loader) })
                // this is needed so spring can locate classes and resources that are on the plugin classpath
                .resourceLoader(VitalResourceLoader())
                .run()
    }

    private fun loadMetadata(classLoader: ClassLoader) {
        log.debug("Loading Vital metadata...")

        val metadataProperties = Properties().apply { load(classLoader.getResourceAsStream(Metadata.FILE_NAME)) }
        metadata = Metadata(metadataProperties[Metadata.Property.MAIN_CLASS].toString())

        log.debug("Vital metadata loaded successfully, {}", metadata)
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

    @Configuration
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val description: String = "A Vital Plugin",
        val apiVersion: String = "1.20",
        val version: String = "1.0",
        val author: Array<String> = [],
        val environment: PluginEnvironment,
        val scanAdditionalPackages: Array<String> = [],
    ) {
        companion object {
            val DEFAULT_PACKAGES = arrayOf("me.vitalframework")
        }

        enum class PluginEnvironment(
            val ymlFileName: String,
        ) {
            SPIGOT("plugin.yml"),
            PAPER("plugin.yml"),
            BUNGEE("bungee.yml"),
        }
    }
}
