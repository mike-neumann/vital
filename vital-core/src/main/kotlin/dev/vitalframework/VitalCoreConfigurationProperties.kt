package dev.vitalframework

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "vital.core")
@Component
class VitalCoreConfigurationProperties(
    val bstats: BStats = BStats(),
) {
    data class BStats(
        var enabled: Boolean = true,
        var pluginId: Int? = null,
    )
}
