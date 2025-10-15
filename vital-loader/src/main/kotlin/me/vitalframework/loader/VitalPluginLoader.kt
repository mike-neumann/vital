package me.vitalframework.loader

import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

interface VitalPluginLoader {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> enable(vitalClassLoader: VitalClassLoader<T>) {
        try {
            // we need to manually load in the "Vital" class to avoid any conflicts with our current class loader...
            // that's also why Vital isn't on the classpath here
            val vital = vitalClassLoader.loadClass("me.vitalframework.Vital")
            val run = vital.getMethod("run", Any::class.java, ClassLoader::class.java)
            run(null, this, vitalClassLoader)
        } catch (e: Exception) {
            println("!!! an error occurred while loading Vital powered plugin!!!")
            println("please consult the following stack trace for any info")
            println("if you think this is a bug, please open an issue on GitHub")
            e.printStackTrace()
        }
    }

    class Spigot :
        JavaPlugin(),
        VitalPluginLoader {
        override fun onEnable() = enable(VitalClassLoader.Spigot(this))
    }

    class Paper :
        JavaPlugin(),
        VitalPluginLoader {
        override fun onEnable() = enable(VitalClassLoader.Paper(this))
    }

    class Bungee :
        Plugin(),
        VitalPluginLoader {
        override fun onEnable() = enable(VitalClassLoader.Bungee(this))
    }
}
