package me.vitalframework

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import java.io.PrintStream
import java.util.*

abstract class Vital {
    companion object {
        lateinit var context: ConfigurableApplicationContext
            private set
    }

    open fun <T : Any> run(plugin: T, configure: () -> SpringApplicationBuilder) {
        val pluginClassLoader = plugin.javaClass.classLoader

        try {
            val properties = Properties().apply {
                load(pluginClassLoader.getResourceAsStream("application.properties"))
            }

            for ((key, value) in properties) {
                System.setProperty(key.toString(), value.toString())
            }
        } catch (_: Exception) {
            // if we haven't defined an application.properties file, we may skip this step
        }

        context = configure()
            .sources(Class.forName("${plugin.javaClass.packageName}.PluginConfiguration"))
            .initializers({
                // here we register the plugin instance as a bean so we can inject it elsewhere
                it.beanFactory.registerSingleton("plugin", plugin)
            })
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

    object Spigot : Vital() {
        fun <T : Any> run(plugin: T) {
            run(plugin) {
                // spigot needs specific class loader configurations
                val pluginClassLoader = plugin.javaClass.classLoader

                Thread.currentThread().contextClassLoader = pluginClassLoader
                ClassUtils.overrideThreadContextClassLoader(pluginClassLoader)

                SpringApplicationBuilder()
                    .initializers({
                        it.classLoader = pluginClassLoader
                    })
                    .resourceLoader(DefaultResourceLoader(pluginClassLoader))
            }
        }
    }

    object Bungee : Vital() {
        fun <T : Any> run(plugin: T) {
            run(plugin) {
                val pluginClassLoader = plugin.javaClass.classLoader

                Thread.currentThread().contextClassLoader = pluginClassLoader
                ClassUtils.overrideThreadContextClassLoader(pluginClassLoader)

                SpringApplicationBuilder()
                    .initializers({
                        it.classLoader = pluginClassLoader
                    })
                    .resourceLoader(DefaultResourceLoader(pluginClassLoader))
            }
        }
    }
}