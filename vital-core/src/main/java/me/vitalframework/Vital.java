package me.vitalframework;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.PrintStream;
import java.util.Properties;

/**
 * The main instance of the Vital-Framework.
 *
 * @author xRa1ny
 */
public class Vital {
    @Getter
    static ConfigurableApplicationContext context;

    /**
     * Initializes the Vital-Framework using Spring Boot
     *
     * @param plugin The plugin instance itself
     */
    @SneakyThrows
    public static <T> void run(@NonNull T plugin) {
        final var pluginClassLoader = plugin.getClass().getClassLoader();

        // needed or else spring startup fails
        Thread.currentThread().setContextClassLoader(pluginClassLoader);

        final var loader = new DefaultResourceLoader(pluginClassLoader);
        final var builder = new SpringApplicationBuilder();
        final var pluginConfiguration = Class.forName(plugin.getClass().getPackageName() + ".PluginConfiguration");

        try {
            final var properties = new Properties();

            properties.load(pluginClassLoader.getResourceAsStream("application.properties"));

            properties.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
        } catch (Exception ignored) {
            // if we haven't defined an application.properties file, we may skip this step
        }

        context = builder.sources(pluginConfiguration)
                .initializers(applicationContext -> {
                    applicationContext.getBeanFactory().registerSingleton("plugin", plugin);
                    applicationContext.setClassLoader(pluginClassLoader);
                    applicationContext.setEnvironment(new StandardEnvironment());
                })
                .resourceLoader(loader)
                .banner(new VitalBanner())
                .logStartupInfo(false)
                .run();
    }

    public static class VitalBanner implements Banner {
        @Override
        public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
            out.print("""
                      .
                     /\\\\ __     ___ _        _  ______ \s
                    ( ( )\\ \\   / ( ) |_ __ _| | \\ \\ \\ \\\s
                     \\\\/  \\ \\ / /| | __/ _` | |  \\ \\ \\ \\
                      ,    \\ V / | | || (_| | |  / / / /
                    ========\\_/==|_|\\__\\__,_|_|=/_/_/_/\s
                                                       \s
                    """);
        }
    }
}