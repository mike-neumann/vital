package me.vitalframework.all

import me.vitalframework.VitalCoreModule.Companion.logger
import me.vitalframework.VitalModule
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
