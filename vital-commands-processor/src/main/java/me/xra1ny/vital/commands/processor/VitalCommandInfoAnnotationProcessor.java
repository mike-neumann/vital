package me.xra1ny.vital.commands.processor;

import lombok.NonNull;
import me.xra1ny.vital.VitalPluginEnvironment;
import me.xra1ny.vital.commands.annotation.VitalCommandInfo;
import me.xra1ny.vital.processor.VitalPluginInfoAnnotationProcessor;
import me.xra1ny.vital.processor.VitalPluginInfoHolder;
import org.reflections.Reflections;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Annotation processor responsible for working with the default {@link VitalPluginInfoAnnotationProcessor} to extend its content with automatic command name registration in plugin.yml.
 *
 * @author xRa1ny
 */
@SupportedAnnotationTypes("*")
public class VitalCommandInfoAnnotationProcessor extends AbstractProcessor {
    private boolean ran;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (ran) {
            return true;
        }

        // Make sure the basic processor for `plugin.yml` meta information generation runs before this one.
        final VitalPluginInfoAnnotationProcessor vitalPluginInfoAnnotationProcessor = new VitalPluginInfoAnnotationProcessor();

        vitalPluginInfoAnnotationProcessor.init(processingEnv);
        vitalPluginInfoAnnotationProcessor.process(annotations, roundEnv);

        final List<VitalCommandInfo> vitalCommandInfoList = new ArrayList<>();

        // Scan for all commands annotated with `VitalCommandInfo.
        for (Element element : roundEnv.getElementsAnnotatedWith(VitalCommandInfo.class)) {
            final VitalCommandInfo vitalCommandInfo = element.getAnnotation(VitalCommandInfo.class);

            if (!vitalCommandInfoList.contains(vitalCommandInfo)) {
                vitalCommandInfoList.add(vitalCommandInfo);
            }
        }

        // also scan all vital packages
        for (Class<?> element : new Reflections("me.xra1ny.vital").getTypesAnnotatedWith(VitalCommandInfo.class, true)) {
            final VitalCommandInfo vitalCommandInfo = element.getDeclaredAnnotation(VitalCommandInfo.class);

            if (!vitalCommandInfoList.contains(vitalCommandInfo)) {
                vitalCommandInfoList.add(vitalCommandInfo);
            }
        }

        // finally generate the `plugin.yml` commands.
        generatePluginYmlCommands(vitalCommandInfoList, vitalPluginInfoAnnotationProcessor.getPluginEnvironment());

        ran = true;

        return true;
    }

    /**
     * Generates the plugin yml if non-existent, or adds to the content, the necessary command name information for automatic command registration.
     *
     * @param vitalCommandInfoList The list of {@link VitalCommandInfo} annotations.
     * @param pluginEnvironment    The environment this plugin uses.
     */
    private void generatePluginYmlCommands(@NonNull List<VitalCommandInfo> vitalCommandInfoList, VitalPluginEnvironment pluginEnvironment) {
        try {
            // Create the new `plugin.yml` file resource as the basic processor left it uncreated.
            final FileObject pluginYmlFileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", pluginEnvironment.getYmlFileName());

            // append all necessary meta-information for all commands to the content builder.
            VitalPluginInfoHolder.PLUGIN_INFO.append("commands:");
            VitalPluginInfoHolder.PLUGIN_INFO.append("\n");

            for (VitalCommandInfo vitalCommandInfo : vitalCommandInfoList) {
                final String vitalCommandName = vitalCommandInfo.name();
                final String vitalCommandDescription = vitalCommandInfo.description();
                final String vitalCommandPermission = vitalCommandInfo.permission();
                final String vitalCommandUsage = vitalCommandInfo.usage();
                final String[] vitalCommandAliases = vitalCommandInfo.aliases();

                VitalPluginInfoHolder.PLUGIN_INFO.append("  ").append(vitalCommandName).append(":");
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
                VitalPluginInfoHolder.PLUGIN_INFO.append("    description: ").append(vitalCommandDescription);
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
                VitalPluginInfoHolder.PLUGIN_INFO.append("    permission: ").append(vitalCommandPermission);
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
                VitalPluginInfoHolder.PLUGIN_INFO.append("    usage: ").append(vitalCommandUsage);
                VitalPluginInfoHolder.PLUGIN_INFO.append("\n");

                if (vitalCommandAliases.length > 0) {
                    VitalPluginInfoHolder.PLUGIN_INFO.append("    aliases: ");
                    VitalPluginInfoHolder.PLUGIN_INFO.append("\n");

                    for (String alias : vitalCommandAliases) {
                        VitalPluginInfoHolder.PLUGIN_INFO.append("      - ").append(alias);
                    }
                }
            }

            // finally write the builder content to the newly created `plugin.yml` resource.
            try (Writer writer = pluginYmlFileObject.openWriter()) {
                writer.write(VitalPluginInfoHolder.PLUGIN_INFO.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while generating plugin yml commands\nIf this error persists, please open an issue on Vital's GitHub page!");
        }
    }
}