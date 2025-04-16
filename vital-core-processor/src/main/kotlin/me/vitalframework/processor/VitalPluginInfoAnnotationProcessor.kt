package me.vitalframework.processor

import me.vitalframework.Vital
import java.io.IOException
import java.io.InputStreamReader
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*")
class VitalPluginInfoAnnotationProcessor : AbstractProcessor() {
    private var ran = false
    lateinit var pluginEnvironment: Vital.Info.PluginEnvironment

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (ran) return true
        var classNameVitalPluginInfoEntry: Pair<String, Vital.Info>? = null

        roundEnv.getElementsAnnotatedWith(Vital.Info::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .forEach {
                val typeElement = it as TypeElement
                val className = typeElement.qualifiedName.toString()
                classNameVitalPluginInfoEntry = className to it.getAnnotation(Vital.Info::class.java)
            }
        // If scan could not resolve the main class, cancel automatic `plugin.yml` creation.
        if (classNameVitalPluginInfoEntry == null) throw VitalPluginInfoAnnotationProcessingException.NoMainClass()
        val className = classNameVitalPluginInfoEntry!!.first
        val pluginInfo = classNameVitalPluginInfoEntry!!.second

        pluginEnvironment = pluginInfo.environment
        // finally generate the `plugin.yml`.
        when (pluginInfo.environment) {
            Vital.Info.PluginEnvironment.SPIGOT -> setupSpigotPluginYml(
                className,
                pluginInfo.name,
                pluginInfo.description,
                pluginInfo.version,
                pluginInfo.apiVersion,
                pluginInfo.author
            )

            Vital.Info.PluginEnvironment.BUNGEE -> setupBungeePluginYml(pluginInfo.name, className, pluginInfo.version, pluginInfo.author)
        }

        generatePluginYml(pluginInfo.environment)
        val packageNames = mutableListOf(*className.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

        packageNames.removeLast()
        // now we have the package name without the class at the end
        val packageName = packageNames.joinToString(".")

        generatePluginConfigurationClass(packageName, pluginInfo.springConfigLocations)

        ran = true

        return true
    }

    fun setupBungeePluginYml(name: String, className: String, version: String, author: Array<String>) {
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("main: me.vitalframework.loader.VitalPluginLoader\$Bungee")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("real-main: $className")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: ${author.contentToString()}")
    }

    fun setupSpigotPluginYml(
        className: String,
        name: String,
        description: String,
        version: String,
        apiVersion: String,
        author: Array<String>,
    ) {
        // append basic plugin meta information to plugin info holder.
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("main: me.vitalframework.loader.VitalPluginLoader\$Spigot")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("real-main: $className")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("description: $description")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("api-version: $apiVersion")
        VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: ${author.contentToString()}")
    }

    fun generatePluginYml(pluginEnvironment: Vital.Info.PluginEnvironment) = try {
        // Scan for the `vital-commands-processor` dependency.
        Class.forName("me.vitalframework.commands.processor.VitalCommandInfoAnnotationProcessor")
        // if found, leave `plugin.yml` creation to `vital-commands-processor`.
    } catch (_: ClassNotFoundException) {
        try {
            // If we couldn't find the dependency, attempt to create the `plugin.yml` ourselves.
            val pluginYmlFileObject = processingEnv.filer.createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                pluginEnvironment.ymlFileName
            )

            pluginYmlFileObject.openWriter().use { it.write(VitalPluginInfoHolder.PLUGIN_INFO.toString()) }
        } catch (e: IOException) {
            throw VitalPluginInfoAnnotationProcessingException.GeneratePluginYml(e)
        }
    }

    fun generatePluginConfigurationClass(packageName: String, springConfigLocations: Array<String>) = try {
        val javaFileObject = processingEnv.filer.createSourceFile("$packageName.PluginConfiguration")
        val resource = VitalPluginInfoAnnotationProcessor::class.java.getResourceAsStream("/Main.java")!!

        javaFileObject.openWriter().use {
            val template = InputStreamReader(resource).readText()

            it.write(
                template
                    .replace("{packageName}", packageName)
                    .replace("{springConfigLocations}", "\"" + springConfigLocations.joinToString(",") + "\"")
                    .replace("{scans}", "\"" + packageName + "\"")
            )
        }
    } catch (e: Exception) {
        throw VitalPluginInfoAnnotationProcessingException.GeneratePluginConfigurationClass(e)
    }
}