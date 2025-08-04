package me.vitalframework.loader

import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader
import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.PluginClassLoader
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile

/**
 * The main internal class loader responsible for loading the custom nested-jar structure of the final plugin jar file.
 * Will load classes and resources from its own classpath first, before delegating to its parent class loader (plugin).
 *
 * Exposes a way to configure parent-first packages via vital.properties.
 *
 * Parent-first packages are needed when multiple class definitions are found on the plugin classpath and nested-jars (dependencies)
 * and type definitions are conflicting between classloaders.
 *
 * @param[classLoaderName] configures which parent-first packages are loaded from vital.properties, e.g.,
 * classLoaderName = "spigot" -> will load the key "vital.classloader.spigot.parent-first-packages" in vital.properties
 */
abstract class VitalClassLoader<T : Any>(classLoaderName: String, val plugin: T, urls: Array<URL>, parent: ClassLoader?) :
    URLClassLoader(urls, parent) {
    val parentFirstPackages = mutableListOf<String>()

    init {
        // since we cannot directly read content out of nested jars (virtual paths),
        // we need to extract them one by one into a temp file
        // after extraction is done we can remove their temp file when the jvm exists (deleteOnExit())
        val pluginFile = File(plugin.javaClass.protectionDomain.codeSource.location.toURI())
        val jarFile = JarFile(pluginFile)
        val jarUrls = jarFile.entries().toList().filter { it.name.endsWith(".jar") }.map {
            val tempFile = File.createTempFile(it.name, ".jar").apply { deleteOnExit() }
            val entryInputStream = jarFile.getInputStream(it)
            tempFile.outputStream().use { entryInputStream.copyTo(it) }
            // now we have copied our virtual .jar file into a temp file,
            // we can now scan the file for its entries / classes
            // and register them on this class loader
            // the temp file will be deleted when this jvm terminates
            tempFile.toURI().toURL()
        }
        jarUrls.forEach(::addURL)
        // now load vital.properties, which can be anywhere on the previously scanned classpath
        val vitalProperties = Properties().apply { load(getResourceAsStream("vital.properties")) }
        val vitalParentFirstPackages =
            (vitalProperties["vital.classloader.${classLoaderName}.parent-first-packages"] as? String?)?.split(",")?.map { it.trim() }
        vitalParentFirstPackages?.forEach(this.parentFirstPackages::add)
    }

    /**
     * exposes a way to manually add urls to the url path of this class loader (delegates to [URLClassLoader.addURL])
     */
    public override fun addURL(url: URL?) {
        super.addURL(url)
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }

        try {
            // some classes MUST be loaded from parent first,
            // to fix some bullshit issues regarding SPIs and plugin class loading
            if (parentFirstPackages.any { name.startsWith(it) }) return super.loadClass(name, resolve)
            val clazz = findClass(name)
            if (resolve) resolveClass(clazz)
            return clazz
        } catch (_: Exception) {
            return super.loadClass(name, resolve)
        }
    }

    override fun getResource(name: String): URL? = findResource(name) ?: parent.getResource(name) ?: parent.parent.getResource(name)

    override fun getResources(name: String): Enumeration<URL> = Collections.enumeration(
        super.getResources(name).toList() + parent.getResources(name).toList() + parent.parent.getResources(name).toList()
    )

    class Spigot(plugin: JavaPlugin) : VitalClassLoader<JavaPlugin>(
        "spigot",
        plugin,
        (plugin.javaClass.classLoader as URLClassLoader).urLs,
        plugin.javaClass.classLoader
    )

    // paper wants to make it as difficult for us as possible,
    // so we must use internal paper components to trick paper into thinking
    // that we are an internal plugin class loader
    @Suppress("UnstableApiUsage")
    class Paper(plugin: JavaPlugin) : VitalClassLoader<JavaPlugin>(
        "paper",
        plugin,
        (plugin.javaClass.classLoader as URLClassLoader).urLs,
        plugin.javaClass.classLoader
    ), ConfiguredPluginClassLoader {
        override fun getConfiguration() = (plugin.javaClass.classLoader as PluginClassLoader).configuration
        override fun loadClass(p0: String, p1: Boolean, p2: Boolean, p3: Boolean): Class<*> =
            (plugin.javaClass.classLoader as PluginClassLoader).loadClass(p0, p1, p2, p3)

        override fun init(p0: JavaPlugin) = (plugin.javaClass.classLoader as PluginClassLoader).init(p0)
        override fun getPlugin() = plugin
        override fun getGroup() = (plugin.javaClass.classLoader as PluginClassLoader).group
    }

    class Bungee(plugin: Plugin) : VitalClassLoader<Plugin>(
        "bungee",
        plugin,
        (plugin.javaClass.classLoader as URLClassLoader).urLs,
        plugin.javaClass.classLoader
    )
}