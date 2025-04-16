package me.vitalframework

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils

class Vital {
    companion object {
        @JvmStatic
        lateinit var context: ConfigurableApplicationContext
            private set

        @JvmStatic
        fun run(loader: Any, pluginClass: Class<*>, classLoader: ClassLoader) {
            Thread.currentThread().contextClassLoader = classLoader
            ClassUtils.overrideThreadContextClassLoader(classLoader)
            // finally start up spring boot, using the previously generated "PluginConfiguration" class as the main class
            context = SpringApplicationBuilder(classLoader.loadClass("${pluginClass.packageName}.PluginConfiguration"))
                // here we register the plugin instance as a bean so we can inject it elsewhere
                .initializers({ it.beanFactory.registerSingleton("plugin", loader) })
                // this is needed so spring can locate classes and resources that are on the plugin classpath
                .resourceLoader(VitalResourceLoader())
                .run()
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
    ) {
        enum class PluginEnvironment(val ymlFileName: String) {
            SPIGOT("plugin.yml"),
            PAPER("plugin.yml"),
            BUNGEE("bungee.yml")
        }
    }
}