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
 * internal vital class loader responsible for loading classes that are contained within the nested jars of the final plugin jar.
 * attempts to load classes and resources from its own classpath first, before delegating to parent (plugin).
 * exposes a way to configure parent-first packages that must be loaded from the parent class loader (plugin), before delegating to child.
 * parent-first packages are needed, when the child has classes on its classpath that are similar to ones found in the parent
 * and types don't match during runtime execution.
 */
abstract class VitalClassLoader<T : Any>(
    plugin: T,
    urls: Array<URL>,
    parent: ClassLoader?,
    private vararg val parentFirstPackages: String,
) : URLClassLoader(urls, parent) {
    init {
        // since we cannot directly read content out of nested jars (virtual paths)
        // we need to extract them one by one into a temp file
        // after extraction is done, and we have scanned every known class,
        // we can remove their temp file when the jvm exists (deleteOnExit())
        val pluginFile = File(plugin.javaClass.protectionDomain.codeSource.location.toURI())
        val jarFile = JarFile(pluginFile)
        val urls = jarFile.entries().toList().filter { it.name.endsWith(".jar") }.map {
            val tempFile = File.createTempFile(it.name, ".jar").apply { deleteOnExit() }
            val entryInputStream = jarFile.getInputStream(it)
            tempFile.outputStream().use { entryInputStream.copyTo(it) }
            // now we have copied our virtual .jar file into a temp file,
            // we can now scan the file for its entries / classes
            // and register them on this class loader
            // the temp file will be deleted when this jvm terminates
            tempFile.toURI().toURL()
        }
        urls.forEach(::addURL)
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        findLoadedClass(name)?.let { return it }

        try {
            // some classes MUST be loaded from parent first,
            // to fix some bullshit issues regarding SPIs and plugin class loading
            if (parentFirstPackages.any { name.startsWith(it) }) super.loadClass(name, resolve)
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
        plugin,
        (plugin.javaClass.classLoader as URLClassLoader).urLs,
        plugin.javaClass.classLoader.parent,
        "org.bukkit.",
        "net.kyori."
    )

    // paper wants to make it as difficult for us as possible,
    // so we must use internal paper components to trick paper into thinking
    // that we are an internal plugin class loader
    @Suppress("UnstableApiUsage")
    class Paper(private val plugin: JavaPlugin) : VitalClassLoader<JavaPlugin>(
        plugin,
        (plugin.javaClass.classLoader as URLClassLoader).urLs,
        plugin.javaClass.classLoader.parent,
    ),
        ConfiguredPluginClassLoader {
        override fun getConfiguration() = (plugin.javaClass.classLoader as PluginClassLoader).configuration
        override fun loadClass(p0: String, p1: Boolean, p2: Boolean, p3: Boolean): Class<*> =
            (plugin.javaClass.classLoader as PluginClassLoader).loadClass(p0, p1, p2, p3)

        override fun init(p0: JavaPlugin) = (plugin.javaClass.classLoader as PluginClassLoader).init(p0)
        override fun getPlugin() = plugin
        override fun getGroup() = (plugin.javaClass.classLoader as PluginClassLoader).group
    }

    class Bungee(plugin: Plugin) : VitalClassLoader<Plugin>(
        plugin,
        (plugin.javaClass.classLoader as URLClassLoader).urLs,
        plugin.javaClass.classLoader,
        "net.md_5.bungee."
    )
}