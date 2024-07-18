package me.xra1ny.vital.spring;

import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(name = "org.bukkit.plugin.java.JavaPlugin")
@Configuration
@ComponentScan(basePackages = "me.xra1ny.vital")
public class VitalSpigotConfiguration {
    @SneakyThrows
    @Bean
    public JavaPlugin spigotPlugin(@Value("${plugin.main}") String pluginMainClassName) {
        return JavaPlugin.getProvidingPlugin(Class.forName(pluginMainClassName));
    }
}
