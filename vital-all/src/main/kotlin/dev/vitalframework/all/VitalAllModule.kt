package dev.vitalframework.all

import dev.vitalframework.VitalCoreModule.Companion.logger
import dev.vitalframework.VitalModule
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Order(Ordered.HIGHEST_PRECEDENCE)
@VitalModule.Info("vital-all")
class VitalAllModule : VitalModule() {
    val logger = logger()

    override fun onEnable() {
        logger.warn(
            "'vital-all' has been enabled, this includes ALL Vital modules, even ones that might not support your current server's runtime",
        )
    }
}
