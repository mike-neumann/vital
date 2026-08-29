package dev.vitalframework.players

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "vital.players")
@Component
class VitalPlayersConfigurationProperties {
    lateinit var playerClassName: String

    @Suppress("UNCHECKED_CAST")
    val playerClass get() = Class.forName(playerClassName) as Class<out VitalPlayer<*>>
}
