package me.vitalframework

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.core.env.StandardEnvironment
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import java.io.PrintStream
import java.util.*

object Vital {
    lateinit var context: ConfigurableApplicationContext
        private set

    fun <T : Any> run(plugin: T) {
        val pluginClassLoader = plugin.javaClass.classLoader
        // load application.properties and profile based ones if they exist
        val applicationProperties = pluginClassLoader.getResourceAsStream("application.properties")?.let { Properties().apply { load(it) } }

        applicationProperties?.forEach { (key, value) -> System.setProperty(key.toString(), value.toString()) }
        val profileApplicationProperties = System.getProperty("spring.profiles.active")?.split(",")
            ?.map { pluginClassLoader.getResourceAsStream("application-${it}.properties") }
            ?.map { it?.let { Properties().apply { load(it) } } }

        profileApplicationProperties?.forEach { it?.forEach { (key, value) -> System.setProperty(key.toString(), value.toString()) } }

        Thread.currentThread().contextClassLoader = pluginClassLoader
        ClassUtils.overrideThreadContextClassLoader(pluginClassLoader)

        context = SpringApplicationBuilder(Class.forName("${plugin.javaClass.packageName}.PluginConfiguration"))
            // here we register the plugin instance as a bean so we can inject it elsewhere
            .initializers({ it.beanFactory.registerSingleton("plugin", plugin) })
            // so system property resolution takes place
            .environment(StandardEnvironment())
            .banner(VitalBanner())
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