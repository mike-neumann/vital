package me.vitalframework

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.*
import org.springframework.util.ClassUtils
import java.util.*

class Vital {
    companion object {
        @JvmStatic
        lateinit var context: ConfigurableApplicationContext
            private set

        @JvmStatic
        fun run(loader: Any, plugin: Any, classLoader: ClassLoader) {
            Thread.currentThread().contextClassLoader = classLoader
            ClassUtils.overrideThreadContextClassLoader(classLoader)
//            // load application.properties and profile based ones if they exist
            val applicationProperties = classLoader.getResourceAsStream("application.properties")?.let { Properties().apply { load(it) } }

            applicationProperties?.forEach { (key, value) -> System.setProperty(key.toString(), value.toString()) }
            val profileApplicationProperties = System.getProperty("spring.profiles.active")?.split(",")
                ?.map { classLoader.getResourceAsStream("application-${it}.properties") }
                ?.map { it?.let { Properties().apply { load(it) } } }

            profileApplicationProperties?.forEach { it?.forEach { (key, value) -> System.setProperty(key.toString(), value.toString()) } }
            //context = SpringApplicationBuilder(Class.forName("${plugin.javaClass.packageName}.PluginConfiguration"))
//            val resourceLoader = object : DefaultResourceLoader(classLoader) {
//                override fun getResource(location: String): Resource {
//                    val found = super.getResource(location)
//                    println("getResource: $location, found $found")
//                    return found
//                }
//            }
            val resourceLoader = object : DefaultResourceLoader(classLoader) {
                override fun getResourceByPath(path: String): Resource {
                    //println("getResourceByPath: $path")
                    return super.getResourceByPath(path)
                }

                override fun getResource(location: String): Resource {
                    fun stripProtocol(path: String) = path.replace(Regex("^[a-zA-Z]+:"), "")

                    val filteredLocation = stripProtocol(location)
                    //println("getResource: $location ($filteredLocation)")
                    // try to get the resource from classloader
                    // if the resource is not found on the classpath of our plugin, delegate to super
                    return getClassLoader()?.getResource(filteredLocation)?.let {
                        var finalLocation = it.path
                        if (finalLocation.contains(".jar!/")) {
                            finalLocation = "jar:$finalLocation"
                        }
                        //println("fetching resource: $finalLocation")
                        UrlResource(finalLocation)
                    } ?: let {
                        //println("using super")
                        super.getResource(location)
                    }
                }
            }
            context = SpringApplicationBuilder(classLoader.loadClass("${plugin.javaClass.packageName}.PluginConfiguration"))
                // here we register the plugin instance as a bean so we can inject it elsewhere
                .initializers({
                    it.beanFactory.registerSingleton("plugin", loader)
                    it.classLoader = classLoader
                })
                .resourceLoader(resourceLoader)
                // so system property resolution takes places
                .environment(StandardEnvironment())
                .run()
            println("beans after: ${context.beanDefinitionNames.contentToString()}")
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
            BUNGEE("bungee.yml")
        }
    }
}