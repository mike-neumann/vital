package me.vitalframework.commands.processor

import me.vitalframework.Vital
import me.vitalframework.commands.VitalCommand
import me.vitalframework.processor.*
import org.reflections.Reflections
import javax.annotation.processing.*
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
        val pluginInfoAnnotationProcessor = VitalPluginInfoAnnotationProcessor().apply {
            init(processingEnv)
            process(annotations, roundEnv)
        }
        val commandInfoList = mutableListOf<VitalCommand.Info>()
        // Scan for all commands annotated with `VitalCommandInfo.
        for (element in roundEnv.getElementsAnnotatedWith(VitalCommand.Info::class.java)) {
            val commandInfo = element.getAnnotation(VitalCommand.Info::class.java)

            if (commandInfo !in commandInfoList) {
                commandInfoList.add(commandInfo!!)
            }
        }

        for (clazz in Reflections("me.vitalframework").getTypesAnnotatedWith(
            VitalCommand.Info::class.java,
            true
        )) {
            val commandInfo = clazz.getDeclaredAnnotation(VitalCommand.Info::class.java)

            if (!commandInfoList.contains(commandInfo)) {
                commandInfoList.add(commandInfo!!)
            }
        }

        generatePluginYmlCommands(commandInfoList, pluginInfoAnnotationProcessor.pluginEnvironment)

        ran = true

        return true
    }

    private fun generatePluginYmlCommands(
        commandInfoList: MutableList<VitalCommand.Info>,
        pluginEnvironment: Vital.Info.PluginEnvironment,
    ) {
        try {
            // Create the new `plugin.yml` file resource as the basic processor left it uncreated.
            val pluginYmlFileObject =
                processingEnv.filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    pluginEnvironment.ymlFileName
                )
            // append all necessary meta-information for all commands to the content builder.
            VitalPluginInfoHolder.PLUGIN_INFO.append("commands:")
            VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

            for (commandInfo in commandInfoList) {
                VitalPluginInfoHolder.PLUGIN_INFO.append("  ").append(commandInfo.name).append(":")
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
                VitalPluginInfoHolder.PLUGIN_INFO.append("    description: ").append(commandInfo.description)
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
                VitalPluginInfoHolder.PLUGIN_INFO.append("    permission: ").append(commandInfo.permission)
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")
                VitalPluginInfoHolder.PLUGIN_INFO.append("    usage: ").append(commandInfo.usage)
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

                if (commandInfo.aliases.isNotEmpty()) {
                    VitalPluginInfoHolder.PLUGIN_INFO.append("    aliases: ")
                    VitalPluginInfoHolder.PLUGIN_INFO.append("\n")

                    for (alias in commandInfo.aliases) {
                        VitalPluginInfoHolder.PLUGIN_INFO.append("      - ").append(alias)
                    }
                }
            }

            pluginYmlFileObject.openWriter().use { it.write(VitalPluginInfoHolder.PLUGIN_INFO.toString()) }
        } catch (e: Exception) {
            throw VitalPluginInfoAnnotationProcessingException.GeneratePluginYml(e)
        }
    }
}