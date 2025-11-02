package me.vitalframework.players

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "vital.players")
@Configuration
class VitalPlayersConfigurationProperties {
    lateinit var playerClassName: String

    @Suppress("UNCHECKED_CAST")
    val playerClass get() = Class.forName(playerClassName) as Class<out VitalPlayer<*>>
}
