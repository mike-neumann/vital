package me.vitalframework.commands.processor

import me.vitalframework.VitalPluginEnvironment
import me.vitalframework.commands.VitalCommand
import me.vitalframework.processor.VitalPluginInfoAnnotationProcessor
import me.vitalframework.processor.VitalPluginInfoHolder
import org.reflections.Reflections
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*")
class VitalCommandInfoAnnotationProcessor : AbstractProcessor() {
    private var ran = false

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (ran) {
            return true
        }

        // Make sure the basic processor runs before this one.
        val vitalPluginInfoAnnotationProcessor = VitalPluginInfoAnnotationProcessor()

        vitalPluginInfoAnnotationProcessor.init(processingEnv)
        vitalPluginInfoAnnotationProcessor.process(annotations, roundEnv)

        val vitalCommandInfoList = ArrayList<VitalCommand.Info>()

        // Scan for all commands annotated with `VitalCommandInfo.
        roundEnv.getElementsAnnotatedWith(VitalCommand.Info::class.java).forEach {
            val vitalCommandInfo = it.getAnnotation(VitalCommand.Info::class.java)

            if (!vitalCommandInfoList.contains(vitalCommandInfo)) {
                vitalCommandInfoList.add(vitalCommandInfo!!)
            }
        }

        Reflections("me.vitalframework").getTypesAnnotatedWith(VitalCommand.Info::class.java, true).forEach {
            val vitalCommandInfo = it.getDeclaredAnnotation(VitalCommand.Info::class.java)

            if (!vitalCommandInfoList.contains(vitalCommandInfo)) {
                vitalCommandInfoList.add(vitalCommandInfo!!)
            }
        }

        generatePluginYmlCommands(vitalCommandInfoList, vitalPluginInfoAnnotationProcessor.pluginEnvironment)

        ran = true

        return true
    }

    private fun generatePluginYmlCommands(
        vitalCommandInfoList: MutableList<VitalCommand.Info>,
        pluginEnvironment: VitalPluginEnvironment,
    ) {
        try {
            // Create the new `plugin.yml` file resource as the basic processor left it uncreated.
            val pluginYmlFileObject =
                processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", pluginEnvironment.ymlFileName)

            // append all necessary meta-information for all commands to the content builder.
            VitalPluginInfoHolder.PLUGIN_INFO.append("commands:")
            VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

            vitalCommandInfoList.forEach {
                val vitalCommandName = it.name
                val vitalCommandDescription = it.description
                val vitalCommandPermission = it.permission
                val vitalCommandUsage = it.usage
                val vitalCommandAliases = it.aliases

                VitalPluginInfoHolder.PLUGIN_INFO.append("  ").append(vitalCommandName).append(":")
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
                VitalPluginInfoHolder.PLUGIN_INFO.append("    description: ").append(vitalCommandDescription)
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
                VitalPluginInfoHolder.PLUGIN_INFO.append("    permission: ").append(vitalCommandPermission)
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
                VitalPluginInfoHolder.PLUGIN_INFO.append("    usage: ").append(vitalCommandUsage)
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

                if (vitalCommandAliases.isNotEmpty()) {
                    VitalPluginInfoHolder.PLUGIN_INFO.append("    aliases: ")
                    VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

                    vitalCommandAliases.forEach {
                        VitalPluginInfoHolder.PLUGIN_INFO.append("      - ").append(it)
                    }
                }
            }

            pluginYmlFileObject.openWriter().use {
                it.write(VitalPluginInfoHolder.PLUGIN_INFO.toString())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("Error while generating plugin yml commands\nIf this error persists, please open an issue on Vital's GitHub page!")
        }
    }
}