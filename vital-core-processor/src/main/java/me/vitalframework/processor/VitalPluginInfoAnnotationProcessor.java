package me.vitalframework.processor;

import lombok.Getter;
import lombok.NonNull;
import me.vitalframework.VitalPluginEnvironment;
import me.vitalframework.VitalPluginInfo;
import org.apache.commons.io.IOUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default Annotation Processor responsible for generating the base information for `plugin.yml` specified by the implementing Plugin via `@VitalPluginInfo`.
 *
 * @author xRa1ny
 */
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*")
public class VitalPluginInfoAnnotationProcessor extends AbstractProcessor {
    private boolean ran;

    @Getter
    private VitalPluginEnvironment pluginEnvironment;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (ran) {
            return true;
        }

        Map.Entry<String, VitalPluginInfo> classNameVitalPluginInfoEntry = null;

        // Scan for the Main Class of this Plugin annotated with `VitalPluginInfo`.
        for (var element : roundEnv.getElementsAnnotatedWith(VitalPluginInfo.class)) {
            var elementKind = element.getKind();

            if (!elementKind.equals(ElementKind.CLASS)) {
                continue;
            }

            final var typeElement = (TypeElement) element;
            final var typeMirror = typeElement.getSuperclass();
            final var typeMirrorName = typeMirror.toString();
            final var spigotPluginMirror = "org.bukkit.plugin.java.JavaPlugin";
            final var bungeecordPluginMirror = "net.md_5.bungee.api.plugin.Plugin";

            if (!typeMirrorName.equals(spigotPluginMirror) && !typeMirrorName.equals(bungeecordPluginMirror)) {
                continue;
            }

            final var className = typeElement.getQualifiedName().toString();

            classNameVitalPluginInfoEntry = Map.entry(className, element.getAnnotation(VitalPluginInfo.class));
        }

        // If scan could not resolve the main class, cancel automatic `plugin.yml` creation.
        if (classNameVitalPluginInfoEntry == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "No Main Plugin Class Found! Main Plugin Class Must Be Annotated With @VitalPluginInfo");

            return false;
        }

        final var className = classNameVitalPluginInfoEntry.getKey();
        final var vitalPluginInfo = classNameVitalPluginInfoEntry.getValue();

        // finally generate the `plugin.yml`.
        generatePluginYml(className, vitalPluginInfo.name(), vitalPluginInfo.apiVersion(), vitalPluginInfo.version(), vitalPluginInfo.environment());

        final var packageNames = new ArrayList<>(List.of(className.split("[.]")));

        packageNames.removeLast();

        // now we have the package name without the class at the end
        final var packageName = String.join(".", packageNames);

        generatePluginConfigurationClass(packageName, vitalPluginInfo.springConfigLocations());

        ran = true;

        return true;
    }

    /**
     * Generates the plugin yml file with the specified meta information.
     *
     * @param className   The fully qualified class name of the main class of this plugin.
     * @param name        The name of this plugin.
     * @param apiVersion  The api-version this plugin uses.
     * @param version     The version of this plugin.
     * @param environment The plugin environment for yml generation.
     */
    public void generatePluginYml(@NonNull String className, @NonNull String name, @NonNull String apiVersion, @NonNull String version, @NonNull VitalPluginEnvironment environment) {
        // append basic plugin meta information to plugin info holder.
        VitalPluginInfoHolder.PLUGIN_INFO.append("main: ").append(className);
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
        VitalPluginInfoHolder.PLUGIN_INFO.append("name: ").append(name);
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
        VitalPluginInfoHolder.PLUGIN_INFO.append("api-version: ").append(apiVersion);
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
        VitalPluginInfoHolder.PLUGIN_INFO.append("version: ").append(version);
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n");
        VitalPluginInfoHolder.PLUGIN_INFO.append("\n");

        try {
            // Scan for the `vital-commands-processor` dependency.
            Class.forName("me.vitalframework.commands.processor.VitalCommandInfoAnnotationProcessor");
            pluginEnvironment = environment;
            // if found, leave `plugin.yml` creation to `vital-commands-processor`.
        } catch (ClassNotFoundException e) {
            try {
                // If we couldn't find the dependency, attempt to create the `plugin.yml` ourselves.
                final var pluginYmlFileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", environment.getYmlFileName());

                // Write the current PluginInfoHolder Information to the newly created `plugin.yml`.
                try (var pluginYmlWriter = pluginYmlFileObject.openWriter()) {
                    pluginYmlWriter.write(VitalPluginInfoHolder.PLUGIN_INFO.toString());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RuntimeException("Error while generating plugin yml\nIf this error persists, please open an issue on Vital's GitHub page!");
            }

        }
    }

    public void generatePluginConfigurationClass(@NonNull String packageName, @NonNull String[] springConfigLocations) {
        try {
            final var javaFileObject = processingEnv.getFiler().createSourceFile(packageName + ".PluginConfiguration");
            final var resource = VitalPluginInfoAnnotationProcessor.class.getResourceAsStream("/Main.java");

            try (var writer = javaFileObject.openWriter()) {
                final var template = IOUtils.toString(new InputStreamReader(resource));

                writer.write(template.replace("{packageName}", packageName).replace("{springConfigLocations}", "\"" + String.join(",", springConfigLocations) + "\"").replace("{scans}", "\"" + packageName + "\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while generating PluginConfiguration class");
        }
    }
}