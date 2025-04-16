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

// this class loader only needs to know its own nested-jars to resolve any dependency classes,
// since the plugin class loader does not know about those.
// own plugin files / classes MUST be loaded by the plugin class loader or else spigot / paper complains about class origins
abstract class VitalClassLoader<T : Any>(plugin: T, private val parentFirstPackages: List<String>) : URLClassLoader(
    (plugin.javaClass.classLoader as URLClassLoader).urLs,
    plugin.javaClass.classLoader.parent
) {
    init {
        // since we cannot directly read content out of nested jars (virtual paths)
        // we need to extract them one by one into a temp file
        // after extraction is done, and we have scanned every known class,
        // we can remove their temp file
        val pluginFile = File(plugin.javaClass.protectionDomain.codeSource.location.toURI())
        val jarFile = JarFile(pluginFile)
        val urls = jarFile.entries().toList().filter { it.name.endsWith(".jar") }.map {
            val tempFile = File.createTempFile(it.name, ".jar").apply { deleteOnExit() }
            val entryInputStream = jarFile.getInputStream(it)
            tempFile.outputStream().use { entryInputStream.copyTo(it) }
            // now we have copied our virtual .jar file into a temp file,
            // we can now scan the file for its entries / classes
            // and register them on this class loader
            tempFile.toURI().toURL()
        }
        urls.forEach(::addURL)
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }

        try {
            // some classes MUST be loaded from parent first,
            // to fix some bullshit issues regarding SPIs and plugin class loading
            if (parentFirstPackages.any { name.startsWith(it) }) {
                return super.loadClass(name, resolve)
            }
            //println("looking for $name")
            val clazz = findClass(name)
            if (resolve) resolveClass(clazz)
            return clazz
        } catch (e: Exception) {
            //println("falling back to parent for $name")
            return super.loadClass(name, resolve)
        }
    }

    override fun getResource(name: String): URL? {
        var found = findResource(name)
        //println("vital found: $found")
        if (found == null) {
            //println("parent is: $parent")
            found = parent.getResource(name)
            //println("plugin found: $found")
        }
        if (found == null) {
            found = parent.parent.getResource(name)
        }
        return found
    }

    override fun getResources(name: String): Enumeration<URL> {
        val urls = super.getResources(name).toList()
        val pluginUrls = parent.getResources(name).toList()
        val parentUrls = parent.parent.getResources(name).toList()
        val allUrls = urls + pluginUrls + parentUrls
        //println("looking for resources $name, found: $allUrls")
        return Collections.enumeration(allUrls)
    }

    // paper wants to make it as difficult for us as possible,
    // so we must use internal paper components to trick paper into thinking
    // that we are in internal plugin class loader
    class Spigot(private val plugin: JavaPlugin) : VitalClassLoader<JavaPlugin>(plugin, listOf("org.bukkit.", "net.kyori.")),
        ConfiguredPluginClassLoader {
        override fun getConfiguration() = (plugin.javaClass.classLoader as PluginClassLoader).configuration
        override fun loadClass(p0: String, p1: Boolean, p2: Boolean, p3: Boolean): Class<*> =
            (plugin.javaClass.classLoader as PluginClassLoader).loadClass(p0, p1, p2, p3)

        override fun init(p0: JavaPlugin) = (plugin.javaClass.classLoader as PluginClassLoader).init(p0)
        override fun getPlugin() = plugin
        override fun getGroup() = (plugin.javaClass.classLoader as PluginClassLoader).group
    }

    // TODO
    class Bungee(plugin: Plugin) : VitalClassLoader<Plugin>(plugin, listOf("net.md_5.bungee", "net.kyori."))
}