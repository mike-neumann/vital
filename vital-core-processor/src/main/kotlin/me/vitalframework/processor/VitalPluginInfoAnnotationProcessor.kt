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

    override fun process(
        annotations: MutableSet<out TypeElement?>,
        roundEnv: RoundEnvironment,
    ): Boolean {
        if (ran) {
            return true
        }
        var classNameVitalPluginInfoEntry: Pair<String, Vital.Info>? = null

        roundEnv.getElementsAnnotatedWith(Vital.Info::class.java)
            .filter { it.kind == ElementKind.CLASS }
            .forEach { element ->
                val typeElement = element as TypeElement
                val typeMirror = typeElement.superclass
                val typeMirrorName = typeMirror.toString()
                val spigotPluginMirror = "org.bukkit.plugin.java.JavaPlugin"
                val bungeecordPluginMirror = "net.md_5.bungee.api.plugin.Plugin"

                if (typeMirrorName != spigotPluginMirror && typeMirrorName != bungeecordPluginMirror) {
                    return@forEach
                }
                val className = typeElement.qualifiedName.toString()

                classNameVitalPluginInfoEntry =
                    className to element.getAnnotation(Vital.Info::class.java)
            }
        // If scan could not resolve the main class, cancel automatic `plugin.yml` creation.
        if (classNameVitalPluginInfoEntry == null) {
            throw VitalPluginInfoAnnotationProcessingException.NoMainClass()
        }
        val className = classNameVitalPluginInfoEntry.first
        val pluginInfo = classNameVitalPluginInfoEntry.second
        // finally generate the `plugin.yml`.
        generatePluginYml(
            className,
            pluginInfo.name,
            pluginInfo.apiVersion,
            pluginInfo.version,
            pluginInfo.environment
        )
        val packageNames =
            mutableListOf(*className.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())

        packageNames.removeLast()
        // now we have the package name without the class at the end
        val packageName = packageNames.joinToString(".")

        generatePluginConfigurationClass(packageName, pluginInfo.springConfigLocations)

        ran = true

        return true
    }

    fun generatePluginYml(
        className: String,
        name: String,
        apiVersion: String,
        version: String,
        environment: Vital.Info.PluginEnvironment,
    ) {
        // append basic plugin meta information to plugin info holder.
        VitalPluginInfoHolder.PLUGIN_INFO.append("main: ").append(className)
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
        VitalPluginInfoHolder.PLUGIN_INFO.append("name: ").append(name)
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
        VitalPluginInfoHolder.PLUGIN_INFO.append("api-version: ").append(apiVersion)
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
        VitalPluginInfoHolder.PLUGIN_INFO.append("version: ").append(version)
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

        try {
            // Scan for the `vital-commands-processor` dependency.
            Class.forName("me.vitalframework.commands.processor.VitalCommandInfoAnnotationProcessor")
            pluginEnvironment = environment
            // if found, leave `plugin.yml` creation to `vital-commands-processor`.
        } catch (e: ClassNotFoundException) {
            try {
                // If we couldn't find the dependency, attempt to create the `plugin.yml` ourselves.
                val pluginYmlFileObject = processingEnv.filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    environment.ymlFileName
                )

                pluginYmlFileObject.openWriter()
                    .use { it.write(VitalPluginInfoHolder.PLUGIN_INFO.toString()) }
            } catch (e: IOException) {
                throw VitalPluginInfoAnnotationProcessingException.GeneratePluginYml(e)
            }
        }
    }

    fun generatePluginConfigurationClass(packageName: String, springConfigLocations: Array<String>) {
        try {
            val javaFileObject = processingEnv.filer.createSourceFile("$packageName.PluginConfiguration")
            val resource = VitalPluginInfoAnnotationProcessor::class.java.getResourceAsStream("/Main.java")!!

            javaFileObject.openWriter().use { writer ->
                val template = InputStreamReader(resource).readText()

                writer.write(
                    template.replace("{packageName}", packageName)
                        .replace(
                            "{springConfigLocations}",
                            "\"" + springConfigLocations.joinToString(",") + "\""
                        )
                        .replace("{scans}", "\"" + packageName + "\"")
                )
            }
        } catch (e: Exception) {
            throw VitalPluginInfoAnnotationProcessingException.GeneratePluginConfigurationClass(e)
        }
    }
}