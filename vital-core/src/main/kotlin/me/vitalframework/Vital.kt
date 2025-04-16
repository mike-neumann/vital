package me.vitalframework

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.*
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
            val resourceLoader = object : DefaultResourceLoader(classLoader) {
                override fun getResourceByPath(path: String): Resource {
                    //println("getResourceByPath: $path")
                    return super.getResourceByPath(path)
                }

                override fun getResource(location: String): Resource {
                    fun stripProtocol(path: String) = path.replace(Regex("^[a-zA-Z]+:"), "")

                    val filteredLocation = stripProtocol(location)
                    // try to get the resource from classloader
                    // if the resource is not found on the classpath of our plugin, delegate to super
                    return getClassLoader()?.getResource(filteredLocation)?.let {
                        var finalLocation = it.path
                        if (finalLocation.contains(".jar!/")) {
                            finalLocation = "jar:$finalLocation"
                        }
                        UrlResource(finalLocation)
                    } ?: super.getResource(location)
                }
            }
            // finally start up spring boot, using the previously generated "PluginConfiguration" class as the main class
            context = SpringApplicationBuilder(classLoader.loadClass("${pluginClass.packageName}.PluginConfiguration"))
                // here we register the plugin instance as a bean so we can inject it elsewhere
                .initializers({ it.beanFactory.registerSingleton("plugin", loader) })
                .resourceLoader(resourceLoader)
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
        val springConfigLocations: Array<String> = ["classpath:application.properties"],
    ) {
        enum class PluginEnvironment(val ymlFileName: String) {
            SPIGOT("plugin.yml"),
            PAPER("plugin.yml"),
            BUNGEE("bungee.yml")
        }
    }
}