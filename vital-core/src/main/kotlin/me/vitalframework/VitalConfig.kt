package me.vitalframework

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class VitalConfig {
    @RequiresSpigot
    @Configuration
    class Spigot {
        // "plugin" is registered when vital starts up, we know it exists, so we can supress the spring warning here
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        @Bean
        fun spigotPlugin(@Qualifier("plugin") plugin: SpigotPlugin) = plugin
    }

    @RequiresBungee
    @Configuration
    class Bungee {
        // "plugin" is registered when vital starts up, we know it exists, so we can supress the spring warning here
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        @Bean
        fun bungeePlugin(@Qualifier("plugin") plugin: BungeePlugin) = plugin
    }
}