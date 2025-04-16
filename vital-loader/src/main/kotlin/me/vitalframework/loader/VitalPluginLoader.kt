package me.vitalframework.loader

import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.*
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.representer.Representer

interface VitalPluginLoader {
    fun <T : Any> enable(vitalClassLoader: VitalClassLoader<T>, pluginYmlFileName: String) {
        try {
            val loaderOptions = LoaderOptions().apply { tagInspector = TagInspector { true } }
            val constructor = Constructor(loaderOptions)
            val dumperOptions = DumperOptions().apply { defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }
            val representer = Representer(dumperOptions)
            val pluginYml = vitalClassLoader.getResourceAsStream(pluginYmlFileName)!!
            val yaml = Yaml(constructor, representer, dumperOptions, loaderOptions)
            val data = yaml.load<Map<String, Any>>(pluginYml)
            val mainClassName = data["real-main"] as String
            val mainClass = vitalClassLoader.loadClass(mainClassName)
            val vital = vitalClassLoader.loadClass("me.vitalframework.Vital")
            val run = vital.getMethod("run", Any::class.java, Class::class.java, ClassLoader::class.java)
            run(null, this, mainClass, vitalClassLoader)
        } catch (e: Exception) {
            println("!!! an error occurred while loading vital powered plugin !!!")
            println("please consult the following stack trace for any info")
            println("if you think this is a bug, please open an issue on github")
            e.printStackTrace()
        }
    }

    class Spigot : JavaPlugin(), VitalPluginLoader {
        override fun onEnable() = enable(VitalClassLoader.Spigot(this), "plugin.yml")
    }

    class Paper : JavaPlugin(), VitalPluginLoader {
        override fun onEnable() = enable(VitalClassLoader.Paper(this), "plugin.yml")
    }

    class Bungee : Plugin(), VitalPluginLoader {
        override fun onEnable() = enable(VitalClassLoader.Bungee(this), "bungee.yml")
    }
}