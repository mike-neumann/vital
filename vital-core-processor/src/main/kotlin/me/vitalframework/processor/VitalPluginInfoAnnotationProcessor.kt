package me.vitalframework.processor

import me.vitalframework.Vital
import java.io.IOException
import java.io.InputStreamReader
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*")
class VitalPluginInfoAnnotationProcessor : AbstractProcessor() {
    private var ran = false
    lateinit var info: Vital.Info

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment,
    ): Boolean {
        // we don't want this to run more than once.
        if (ran) {
            return true
        }

        val mainClassNamesAndInfo = getMainClassNamesAndInfo(roundEnv)
        if (mainClassNamesAndInfo.size > 1) {
            throw VitalPluginInfoAnnotationProcessingException.MultipleMainClasses(*mainClassNamesAndInfo.map { it.first }.toTypedArray())
        }

        val (className, info) = mainClassNamesAndInfo.firstOrNull() ?: throw VitalPluginInfoAnnotationProcessingException.NoMainClass()
        writeMetadataFile(className)

        this.info = info
        setupPluginYml(
            info.environment,
            info.name,
            info.description,
            info.version,
            info.apiVersion,
            info.author,
        )
        generatePluginYml(info.environment)

        val packageName = className.substringBeforeLast(".")
        generatePluginConfigurationClass(packageName)

        ran = true
        return true
    }

    private fun getMainClassNamesAndInfo(roundEnv: RoundEnvironment): List<Pair<String, Vital.Info>> =
        roundEnv
            .getElementsAnnotatedWith(Vital.Info::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .map {
                val typeElement = it as TypeElement
                val className = typeElement.qualifiedName.toString()
                className to it.getAnnotation(Vital.Info::class.java)
            }

    private fun writeMetadataFile(mainClass: String) {
        processingEnv.filer
            .createResource(StandardLocation.CLASS_OUTPUT, "", Vital.Metadata.FILE_NAME)
            .openWriter()
            .use { it.write(Vital.Metadata(mainClass).serialize()) }
    }

    private fun setupPluginYml(
        pluginEnvironment: Vital.Info.PluginEnvironment,
        name: String,
        description: String,
        version: String,
        apiVersion: String,
        author: Array<String>,
    ) {
        when (pluginEnvironment) {
            Vital.Info.PluginEnvironment.BUNGEE -> {
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine($$"main: me.vitalframework.loader.VitalPluginLoader$Bungee")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: ${author.contentToString()}")
            }

            Vital.Info.PluginEnvironment.SPIGOT, Vital.Info.PluginEnvironment.PAPER -> {
                val vitalPluginLoaderImplementationName =
                    if (pluginEnvironment == Vital.Info.PluginEnvironment.PAPER) "Paper" else "Spigot"
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine(
                    "main: me.vitalframework.loader.VitalPluginLoader$$vitalPluginLoaderImplementationName",
                )
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("description: $description")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("api-version: $apiVersion")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: ${author.contentToString()}")
            }
        }
    }

    private fun generatePluginYml(pluginEnvironment: Vital.Info.PluginEnvironment) =
        try {
            // scan for the vital-commands-processor dependency.
            Class.forName("me.vitalframework.commands.processor.VitalCommandInfoAnnotationProcessor")
            // if found, leave plugin.yml creation to vital-commands-processor.
        } catch (_: ClassNotFoundException) {
            try {
                // If we couldn't find the dependency, attempt to create the plugin.yml ourselves.
                val pluginYmlFileObject =
                    processingEnv.filer.createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        pluginEnvironment.ymlFileName,
                    )
                pluginYmlFileObject.openWriter().use { it.write(VitalPluginInfoHolder.PLUGIN_INFO.toString()) }
            } catch (e: IOException) {
                throw VitalPluginInfoAnnotationProcessingException.GeneratePluginYml(e)
            }
        }

    private fun generatePluginConfigurationClass(packageName: String) =
        try {
            val javaFileObject = processingEnv.filer.createSourceFile("$packageName.PluginConfiguration")
            val resource = VitalPluginInfoAnnotationProcessor::class.java.getResourceAsStream("/Main.java")!!
            javaFileObject.openWriter().use {
                val template = InputStreamReader(resource).readText()
                it.write(template.replace($$"${packageName}", packageName).replace($$"${scans}", "\"$packageName\""))
            }
        } catch (e: Exception) {
            throw VitalPluginInfoAnnotationProcessingException.GeneratePluginConfigurationClass(e)
        }
}
