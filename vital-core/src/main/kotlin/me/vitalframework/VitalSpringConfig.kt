package me.vitalframework

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Main spring configuration for the implementing plugin.
 * Exposes the plugin as a bean to fix classpath scanning errors when ide scans for beans.
 */
class VitalSpringConfig {
    @ComponentScan(basePackages = ["me.vitalframework"])
    @RequiresSpigot
    @Configuration
    class Spigot {
        // "plugin" is registered when vital starts up, we know it exists, so we can suppress the spring warning here
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        @Bean
        fun spigotPlugin(@Qualifier("plugin") plugin: SpigotPlugin) = plugin
    }

    @ComponentScan(basePackages = ["me.vitalframework"])
    @RequiresBungee
    @Configuration
    class Bungee {
        // "plugin" is registered when vital starts up, we know it exists, so we can suppress the spring warning here
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        @Bean
        fun bungeePlugin(@Qualifier("plugin") plugin: BungeePlugin) = plugin
    }
}