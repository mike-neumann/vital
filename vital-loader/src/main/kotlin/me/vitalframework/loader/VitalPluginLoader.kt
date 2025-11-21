package me.vitalframework.loader

import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

interface VitalPluginLoader {
    val vitalClassLoader: VitalClassLoader<*>

    fun enable() {
        try {
            // we need to manually load in the "Vital" class to avoid any conflicts with our current class loader...
            // that's also why Vital isn't on the classpath here
            val vital = vitalClassLoader.loadClass("me.vitalframework.Vital")
            val run = vital.getMethod("run", Any::class.java, ClassLoader::class.java)
            run(null, this, vitalClassLoader)
        } catch (e: Exception) {
            println("!!! an error occurred while enabling Vital powered plugin!!!")
            println("please consult the following stack trace for any info")
            println("if you think this is a bug, please open an issue on GitHub")
            e.printStackTrace()
        }
    }

    fun disable() {
        try {
            // we need to manually load in the "Vital" class to avoid any conflicts with our current class loader...
            // that's also why Vital isn't on the classpath here
            val vital = vitalClassLoader.loadClass("me.vitalframework.Vital")
            val exit = vital.getMethod("exit")
            exit(null)
        } catch (e: Exception) {
            println("!!! an error occurred while disabling Vital powered plugin!!!")
            println("please consult the following stack trace for any info")
            println("if you think this is a bug, please open an issue on GitHub")
            e.printStackTrace()
        }
    }

    class Spigot :
        JavaPlugin(),
        VitalPluginLoader {
        override val vitalClassLoader = VitalClassLoader.Spigot(this)

        override fun onEnable() {
            enable()
        }

        override fun onDisable() {
            disable()
        }
    }

    class Paper :
        JavaPlugin(),
        VitalPluginLoader {
        override val vitalClassLoader = VitalClassLoader.Paper(this)

        override fun onEnable() {
            enable()
        }

        override fun onDisable() {
            disable()
        }
    }

    class Bungee :
        Plugin(),
        VitalPluginLoader {
        override val vitalClassLoader = VitalClassLoader.Bungee(this)

        override fun onEnable() {
            enable()
        }

        override fun onDisable() {
            disable()
        }
    }
}
