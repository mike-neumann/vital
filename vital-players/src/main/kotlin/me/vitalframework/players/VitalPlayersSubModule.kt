package me.vitalframework.players

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-players")
class VitalPlayersSubModule(
    val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.info("Using class '${vitalPlayersConfigurationProperties.playerClassName}' for new Vital managed player instances")
    }
}
