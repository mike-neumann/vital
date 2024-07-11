package me.xra1ny.vital;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * The main instance of the Vital-Framework.
 *
 * @author xRa1ny
 */
public class Vital {
    @Getter
    private static ConfigurableApplicationContext context;

    /**
     * Initializes the Vital-Framework using Spring Boot
     *
     * @param plugin The plugin instance itself
     */
    @SneakyThrows
    public static void run(Object plugin) {
        final SpringApplicationBuilder builder = new SpringApplicationBuilder();
        final ClassLoader classLoader = plugin.getClass().getClassLoader();
        final Class<?> pluginConfig = Class.forName(plugin.getClass().getPackageName() + ".PluginConfiguration");
        final Class<?>[] sources = {pluginConfig, VitalCoreSubModule.class};
        final ResourceLoader loader = new DefaultResourceLoader(classLoader);

        Thread.currentThread().setContextClassLoader(classLoader);
        context = builder.sources(sources)
                // register a single bean called pluginInstance, for any global access directly to the plugin instance Vital is currently running with
                .initializers(context -> context.getBeanFactory().registerSingleton("pluginInstance", plugin))
                .web(WebApplicationType.NONE)
                .resourceLoader(loader)
                .run();
    }
}