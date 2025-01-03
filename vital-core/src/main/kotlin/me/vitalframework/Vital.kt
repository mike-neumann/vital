package me.vitalframework

import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.stereotype.Component
import java.io.PrintStream
import java.util.*

typealias SpigotEventHandler = EventHandler
typealias BungeeEventHandler = net.md_5.bungee.event.EventHandler
typealias SpigotCommandSender = CommandSender
typealias BungeeCommandSender = net.md_5.bungee.api.CommandSender
typealias SpigotPlugin = Plugin
typealias BungeePlugin = net.md_5.bungee.api.plugin.Plugin
typealias SpigotListener = Listener
typealias BungeeListener = net.md_5.bungee.api.plugin.Listener
typealias SpigotPlayer = org.bukkit.entity.Player
typealias BungeePlayer = net.md_5.bungee.api.connection.ProxiedPlayer
typealias SpigotEventPriority = org.bukkit.event.EventPriority
typealias BungeeEventPriority = net.md_5.bungee.event.EventPriority

/**
 * The main instance of the Vital-Framework.
 */
object Vital {
    lateinit var context: ConfigurableApplicationContext

    fun <T : Any> run(plugin: T) {
        val pluginClassLoader: ClassLoader = plugin::class.java.getClassLoader()

        // needed or else spring startup fails
        Thread.currentThread().contextClassLoader = pluginClassLoader

        val loader = DefaultResourceLoader(pluginClassLoader)
        val builder = SpringApplicationBuilder()
        val pluginConfiguration = Class.forName(plugin::class.java.getPackageName() + ".PluginConfiguration")

        try {
            val properties = Properties()

            properties.load(pluginClassLoader.getResourceAsStream("application.properties"))

            properties.forEach { (key: Any?, value: Any?) -> System.setProperty(key.toString(), value.toString()) }
        } catch (ignored: Exception) {
            // if we haven't defined an application.properties file, we may skip this step
        }

        context = builder.sources(pluginConfiguration)
            .initializers({
                // here we register the plugin instance as a bean so we can inject it elsewhere
                it.beanFactory.registerSingleton("plugin", plugin)
                it.classLoader = pluginClassLoader
                it.environment = StandardEnvironment()
            })
            .resourceLoader(loader)
            .banner(VitalBanner())
            .logStartupInfo(false)
            .run()
    }

    class VitalBanner : Banner {
        override fun printBanner(environment: Environment, sourceClass: Class<*>?, out: PrintStream) {
            out.print(
                """
                      .
                     /\\ __     ___ _        _  ______  
                    ( ( )\ \   / ( ) |_ __ _| | \ \ \ \ 
                     \\/  \ \ / /| | __/ _` | |  \ \ \ \
                      ,    \ V / | | || (_| | |  / / / /
                    ========\_/==|_|\__\__,_|_|=/_/_/_/ 
                                                        
                    
                                                        
                    """.trimIndent()
            )
        }
    }

    /**
     * Specifies metadata for the projects plugin implementations.
     */
    @ConditionalOnBean(name = ["plugin"])
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        /**
         * Defines the name of this plugin.
         */
        val name: String,

        /**
         * Defines the description of this plugin.
         */
        val description: String = "A Vital Plugin",

        /**
         * Defines the api version this plugin uses.
         */
        val apiVersion: String = "1.20",

        /**
         * Defines the version of this plugin.
         */
        val version: String = "1.0",

        /**
         * The author/s of this plugin.
         */
        val authors: String = "",

        /**
         * Defines this vital plugin instance environment for automatic plugin yml generation.
         */
        val environment: VitalPluginEnvironment,

        /**
         * Defines the locations where spring should look for configuration files.
         */
        val springConfigLocations: Array<String> = ["classpath:application.properties"],
    )

    fun <T : Any> T.log(): Logger = LoggerFactory.getLogger(this::class.java)
}