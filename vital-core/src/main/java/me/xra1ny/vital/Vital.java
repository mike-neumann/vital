package me.xra1ny.vital;

import lombok.Getter;
import lombok.SneakyThrows;
import me.xra1ny.vital.spring.VitalBanner;
import me.xra1ny.vital.spring.VitalBungeecordConfiguration;
import me.xra1ny.vital.spring.VitalSpigotConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

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
        Thread.currentThread().setContextClassLoader(plugin.getClassLoader());

        final SpringApplicationBuilder builder = new SpringApplicationBuilder();
        final Class<?> pluginConfiguration = Class.forName(plugin.getPackageName() + ".PluginConfiguration");
        final Class<?>[] sources = {pluginConfiguration, VitalSpigotConfiguration.class, VitalBungeecordConfiguration.class};

        context = builder.sources(sources)
                .resourceLoader(new DefaultResourceLoader(plugin.getClassLoader()))
                .logStartupInfo(false)
                .banner(new VitalBanner())
                .properties("plugin.name=" + pluginName)
                .properties("plugin.main=" + plugin.getName())
                .run();
    }
}