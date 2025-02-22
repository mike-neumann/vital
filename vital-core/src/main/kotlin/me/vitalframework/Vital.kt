package me.vitalframework

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.stereotype.Component
import java.io.PrintStream
import java.util.*

object Vital {
    lateinit var context: ConfigurableApplicationContext

    fun <T : Any> run(plugin: T) {
        val pluginClassLoader = plugin::class.java.getClassLoader()
        val loader = DefaultResourceLoader(pluginClassLoader)
        val builder = SpringApplicationBuilder()
        val pluginConfiguration =
            Class.forName(plugin::class.java.getPackageName() + ".PluginConfiguration")

        try {
            val properties = Properties().apply {
                load(pluginClassLoader.getResourceAsStream("application.properties"))
            }

            for ((key, value) in properties) {
                System.setProperty(key.toString(), value.toString())
            }
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

    @ConditionalOnBean(name = ["plugin"])
    @Component
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Info(
        val name: String,
        val description: String = "A Vital Plugin",
        val apiVersion: String = "1.20",
        val version: String = "1.0",
        val author: Array<String> = [],
        val environment: PluginEnvironment,
        val springConfigLocations: Array<String> = ["classpath:application.properties"],
    ) {
        enum class PluginEnvironment(val ymlFileName: String) {
            SPIGOT("plugin.yml"),
            BUNGEE("bungee.yml")
        }
    }
}