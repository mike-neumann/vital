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
        // try to resolve the main class, if not found, throw exception
        val (className, info) = roundEnv.getElementsAnnotatedWith(Vital.Info::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .map {
                val typeElement = it as TypeElement
                val className = typeElement.qualifiedName.toString()
                className to it.getAnnotation(Vital.Info::class.java)
            }.firstOrNull() ?: throw VitalPluginInfoAnnotationProcessingException.NoMainClass()

        pluginEnvironment = info.environment
        // finally, generate the plugin.yml
        setupPluginYml(info.environment, className, info.name, info.description, info.version, info.apiVersion, info.author)
        generatePluginYml(info.environment)
        val packageNames = className.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toMutableList().apply { removeLast() }
        // now we have the package name without the class at the end
        val packageName = packageNames.joinToString(".")

        generatePluginConfigurationClass(packageName)

        return true.also { ran = true }
    }

    private fun setupPluginYml(
        pluginEnvironment: Vital.Info.PluginEnvironment,
        className: String,
        name: String,
        description: String,
        version: String,
        apiVersion: String,
        author: Array<String>,
    ) {
        when (pluginEnvironment) {
            Vital.Info.PluginEnvironment.BUNGEE -> {
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("main: me.vitalframework.loader.VitalPluginLoader\$Bungee")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("real-main: $className")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: ${author.contentToString()}")
            }

            Vital.Info.PluginEnvironment.SPIGOT, Vital.Info.PluginEnvironment.PAPER -> {
                val vitalPluginLoaderImplementationName = if (pluginEnvironment == Vital.Info.PluginEnvironment.PAPER) "Paper" else "Spigot"
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("main: me.vitalframework.loader.VitalPluginLoader$$vitalPluginLoaderImplementationName")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("real-main: $className")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("description: $description")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("api-version: $apiVersion")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: ${author.contentToString()}")
            }
        }
    }

    private fun generatePluginYml(pluginEnvironment: Vital.Info.PluginEnvironment) = try {
        // scan for the vital-commands-processor dependency.
        Class.forName("me.vitalframework.commands.processor.VitalCommandInfoAnnotationProcessor")
        // if found, leave plugin.yml creation to vital-commands-processor.
    } catch (_: ClassNotFoundException) {
        try {
            // If we couldn't find the dependency, attempt to create the plugin.yml ourselves.
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

    private fun generatePluginConfigurationClass(packageName: String) = try {
        val javaFileObject = processingEnv.filer.createSourceFile("$packageName.PluginConfiguration")
        val resource = VitalPluginInfoAnnotationProcessor::class.java.getResourceAsStream("/Main.java")!!
        javaFileObject.openWriter().use {
            val template = InputStreamReader(resource).readText()
            it.write(template.replace("\${packageName}", packageName).replace("\${scans}", "\"$packageName\""))
        }
    } catch (e: Exception) {
        throw VitalPluginInfoAnnotationProcessingException.GeneratePluginConfigurationClass(e)
    }
}