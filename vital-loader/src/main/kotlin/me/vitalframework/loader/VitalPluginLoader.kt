package me.vitalframework.loader

import net.md_5.bungee.api.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.*
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.inspector.TagInspector
import org.yaml.snakeyaml.representer.Representer

interface VitalPluginLoader {
    fun <T : Any> enable(vitalClassLoader: VitalClassLoader<T>, mainClassName: String) {
        try {
            val mainClass = vitalClassLoader.loadClass(mainClassName)
            val plugin = mainClass.getConstructor().newInstance()
            val vital = vitalClassLoader.loadClass("me.vitalframework.Vital")
            val run = vital.getMethod("run", Any::class.java, Any::class.java, ClassLoader::class.java)
            run(null, this, plugin, vitalClassLoader)
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

    class Spigot : JavaPlugin(), VitalPluginLoader {
        override fun onEnable() {
            val loaderOptions = LoaderOptions().apply { tagInspector = TagInspector { true } }
            val constructor = Constructor(loaderOptions)
            val dumperOptions = DumperOptions().apply { defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }
            val representer = Representer(dumperOptions)
            val pluginYml = getResource("plugin.yml")!!
            val yaml = Yaml(constructor, representer, dumperOptions, loaderOptions)
            val data = yaml.load<Map<String, String>>(pluginYml)
            val realMain = data["real-main"] as String
            enable(VitalClassLoader.Spigot(this), realMain)
        }
    }

    class Bungee : Plugin(), VitalPluginLoader {
        override fun onEnable() {
            val loaderOptions = LoaderOptions().apply { tagInspector = TagInspector { true } }
            val constructor = Constructor(loaderOptions)
            val dumperOptions = DumperOptions().apply { defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }
            val representer = Representer(dumperOptions)
            val pluginYml = getResourceAsStream("bungee.yml")!!
            val yaml = Yaml(constructor, representer, dumperOptions, loaderOptions)
            val data = yaml.load<Map<String, String>>(pluginYml)
            val realMain = data["real-main"] as String
            enable(VitalClassLoader.Bungee(this), realMain)
        }
    }
}