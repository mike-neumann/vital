package me.vitalframework.all

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component("vital-all")
class VitalAllSubModule : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.warn(
            "'vital-all' has been installed, this includes ALL Vital submodules, even ones that might not support your current server's runtime",
        )
    }
}
