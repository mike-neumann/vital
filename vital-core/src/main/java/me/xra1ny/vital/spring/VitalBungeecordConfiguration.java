package me.xra1ny.vital.spring;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(name = "net.md_5.bungee.api.plugin.Plugin")
@Configuration
@ComponentScan(basePackages = "me.xra1ny.vital")
public class VitalBungeecordConfiguration {
    @Bean
    public Plugin bungeePlugin(@Value("${plugin.name}") String pluginName) {
        return ProxyServer.getInstance().getPluginManager().getPlugin(pluginName);
    }
}
