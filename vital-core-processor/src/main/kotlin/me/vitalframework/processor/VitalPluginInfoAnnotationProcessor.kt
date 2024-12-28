package me.vitalframework.processor

import me.vitalframework.Vital
import me.vitalframework.VitalPluginEnvironment
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.InputStreamReader
import java.util.Map
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*")
class VitalPluginInfoAnnotationProcessor : AbstractProcessor() {
    private var ran = false
    lateinit var pluginEnvironment: VitalPluginEnvironment

    override fun process(annotations: MutableSet<out TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (ran) {
            return true
        }

        var classNameVitalPluginInfoEntry: MutableMap.MutableEntry<String, Vital.Info>? = null

        roundEnv.getElementsAnnotatedWith(Vital.Info::class.java)
            .filter { it.kind != ElementKind.CLASS }
            .forEach {
                val typeElement = it as TypeElement
                val typeMirror = typeElement.superclass
                val typeMirrorName = typeMirror.toString()
                val spigotPluginMirror = "org.bukkit.plugin.java.JavaPlugin"
                val bungeecordPluginMirror = "net.md_5.bungee.api.plugin.Plugin"

                if (typeMirrorName != spigotPluginMirror && typeMirrorName != bungeecordPluginMirror) {
                    return@forEach
                }

                val className = typeElement.qualifiedName.toString()

                classNameVitalPluginInfoEntry = Map.entry(className, it.getAnnotation(Vital.Info::class.java))
            }

        // If scan could not resolve the main class, cancel automatic `plugin.yml` creation.
        if (classNameVitalPluginInfoEntry == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "No Main Plugin Class Found! Main Plugin Class Must Be Annotated With @VitalPluginInfo"
            )

            return false
        }

        val className = classNameVitalPluginInfoEntry.key
        val vitalPluginInfo = classNameVitalPluginInfoEntry.value

        // finally generate the `plugin.yml`.
        generatePluginYml(
            className,
            vitalPluginInfo.name,
            vitalPluginInfo.apiVersion,
            vitalPluginInfo.version,
            vitalPluginInfo.environment
        )

        val packageNames =
            mutableListOf(*className.split("[.]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

        packageNames.removeLast()

        // now we have the package name without the class at the end
        val packageName = packageNames.joinToString(".")

        generatePluginConfigurationClass(packageName, vitalPluginInfo.springConfigLocations)

        ran = true

        return true
    }

    fun generatePluginYml(
        className: String,
        name: String,
        apiVersion: String,
        version: String,
        environment: VitalPluginEnvironment,
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
                val pluginYmlFileObject =
                    processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", environment.ymlFileName)

                pluginYmlFileObject.openWriter().use { pluginYmlWriter ->
                    pluginYmlWriter.write(VitalPluginInfoHolder.PLUGIN_INFO.toString())
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
                throw RuntimeException("Error while generating plugin yml\nIf this error persists, please open an issue on Vital's GitHub page!")
            }
        }
    }

    fun generatePluginConfigurationClass(packageName: String, springConfigLocations: Array<String>) {
        try {
            val javaFileObject = processingEnv.filer.createSourceFile("$packageName.PluginConfiguration")
            val resource = VitalPluginInfoAnnotationProcessor::class.java.getResourceAsStream("/Main.java")!!

            javaFileObject.openWriter().use {
                val template = IOUtils.toString(InputStreamReader(resource))
                it.write(
                    template.replace("{packageName}", packageName)
                        .replace("{springConfigLocations}", "\"" + springConfigLocations.joinToString(",") + "\"")
                        .replace("{scans}", "\"" + packageName + "\"")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error while generating PluginConfiguration class")
        }
    }
}