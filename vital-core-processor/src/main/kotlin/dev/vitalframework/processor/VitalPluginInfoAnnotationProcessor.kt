package dev.vitalframework.processor

import dev.vitalframework.VitalPlugin
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

@SupportedSourceVersion(SourceVersion.RELEASE_24)
@SupportedAnnotationTypes("*")
class VitalPluginInfoAnnotationProcessor : AbstractProcessor() {
    private var ran = false
    lateinit var info: VitalPlugin.Info
    lateinit var pluginEnvironment: VitalPlugin.PluginEnvironment

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment,
    ): Boolean {
        // we don't want this to run more than once.
        if (ran) {
            return true
        }

        val mainClassNamesAndInfo = getMainClassAndInfo(roundEnv)
        if (mainClassNamesAndInfo.size > 1) {
            throw VitalPluginInfoAnnotationProcessingException.MultipleMainClasses(
                *mainClassNamesAndInfo
                    .map {
                        it.first.qualifiedName.toString()
                    }.toTypedArray(),
            )
        }

        val (className, info) = mainClassNamesAndInfo.firstOrNull() ?: throw VitalPluginInfoAnnotationProcessingException.NoMainClass()
        val classNameType = className.asType()
        val isVitalPluginSpigotSubtype =
            processingEnv.typeUtils.isSubtype(
                classNameType,
                processingEnv.elementUtils.getTypeElement("dev.vitalframework.VitalPlugin.Spigot").asType(),
            )
        val isVitalPluginPaperSubtype =
            processingEnv.typeUtils.isSubtype(
                classNameType,
                processingEnv.elementUtils.getTypeElement("dev.vitalframework.VitalPlugin.Paper").asType(),
            )
        val isVitalPluginBungeeSubtype =
            processingEnv.typeUtils.isSubtype(
                classNameType,
                processingEnv.elementUtils.getTypeElement("dev.vitalframework.VitalPlugin.Bungee").asType(),
            )

        pluginEnvironment =
            if (isVitalPluginSpigotSubtype) {
                VitalPlugin.PluginEnvironment.SPIGOT
            } else if (isVitalPluginPaperSubtype) {
                VitalPlugin.PluginEnvironment.PAPER
            } else if (isVitalPluginBungeeSubtype) {
                VitalPlugin.PluginEnvironment.BUNGEE
            } else {
                throw VitalPluginInfoAnnotationProcessingException.InvalidMainPluginClassType(className.qualifiedName.toString())
            }

        writeMetadataFile(className.qualifiedName.toString())

        this.info = info
        setupPluginYml(
            pluginEnvironment,
            info.name,
            info.description,
            info.version,
            info.apiVersion,
            info.author,
        )
        generatePluginYml(pluginEnvironment)

        val packageName = className.qualifiedName.toString().substringBeforeLast(".")
        generatePluginConfigurationClass(packageName)

        ran = true
        return true
    }

    private fun getMainClassAndInfo(roundEnv: RoundEnvironment): List<Pair<TypeElement, VitalPlugin.Info>> =
        roundEnv
            .getElementsAnnotatedWith(VitalPlugin.Info::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .map { it as TypeElement to it.getAnnotation(VitalPlugin.Info::class.java)!! }

    private fun writeMetadataFile(mainClass: String) {
        processingEnv.filer
            .createResource(StandardLocation.CLASS_OUTPUT, "", VitalPlugin.Metadata.FILE_NAME)
            .openWriter()
            .use { it.write(VitalPlugin.Metadata(mainClass).serialize()) }
    }

    private fun setupPluginYml(
        pluginEnvironment: VitalPlugin.PluginEnvironment,
        name: String,
        description: String,
        version: String,
        apiVersion: String,
        author: Array<String>,
    ) {
        when (pluginEnvironment) {
            VitalPlugin.PluginEnvironment.BUNGEE -> {
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine($$"main: dev.vitalframework.loader.VitalPluginLoader$Bungee")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: \"${author.joinToString(", ")}\"")
            }

            VitalPlugin.PluginEnvironment.SPIGOT, VitalPlugin.PluginEnvironment.PAPER -> {
                val vitalPluginLoaderImplementationName =
                    if (pluginEnvironment == VitalPlugin.PluginEnvironment.PAPER) "Paper" else "Spigot"
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine(
                    "main: dev.vitalframework.loader.VitalPluginLoader$$vitalPluginLoaderImplementationName",
                )
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("name: $name")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("version: $version")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("description: $description")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("api-version: $apiVersion")
                VitalPluginInfoHolder.PLUGIN_INFO.appendLine("author: [${author.joinToString(", ") { "\"${it}\"" }}]")
            }
        }
    }

    private fun generatePluginYml(pluginEnvironment: VitalPlugin.PluginEnvironment) =
        try {
            // scan for the vital-commands-processor dependency.
            Class.forName("dev.vitalframework.commands.processor.VitalCommandInfoAnnotationProcessor")
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
