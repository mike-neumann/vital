package me.vitalframework;

import lombok.Getter;
import lombok.SneakyThrows;
import me.vitalframework.spring.VitalBanner;
import me.vitalframework.spring.VitalBungeecordConfiguration;
import me.vitalframework.spring.VitalSpigotConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Properties;

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
    public static void run(Class<?> plugin, String pluginName) {
        final ClassLoader pluginClassLoader = plugin.getClassLoader();
        // needed or else spring startup fails
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        final ResourceLoader loader = new DefaultResourceLoader(pluginClassLoader);
        final SpringApplicationBuilder builder = new SpringApplicationBuilder();
        final Class<?> pluginConfiguration = Class.forName(plugin.getPackageName() + ".PluginConfiguration");
        final Class<?>[] sources = {pluginConfiguration, VitalSpigotConfiguration.class, VitalBungeecordConfiguration.class};

        System.setProperty("plugin.name", pluginName);
        System.setProperty("plugin.main", plugin.getName());

        try {
            final Properties properties = new Properties();

            properties.load(pluginClassLoader.getResourceAsStream("application.properties"));

            properties.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
        } catch (Exception ignored) {
        }

        context = builder.sources(sources)
                .initializers(applicationContext -> {
                    applicationContext.setClassLoader(pluginClassLoader);
                    applicationContext.setEnvironment(new StandardEnvironment());
                })
                .resourceLoader(loader)
                .banner(new VitalBanner())
                .logStartupInfo(false)
                .run();
    }
}