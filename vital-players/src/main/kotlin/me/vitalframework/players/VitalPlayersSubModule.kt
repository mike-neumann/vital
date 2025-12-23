package me.vitalframework.players

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

/**
 * Defines the official vital-players submodule, which is displayed when Vital starts.
 * It contains the Vital players system, which can be used to get a custom player-management solution,
 * useful when you need to store player-specific data on its own isolated instance, which can later be retrieved at any time.
 */
@SubModule("vital-players")
class VitalPlayersSubModule(
    val vitalPlayersConfigurationProperties: VitalPlayersConfigurationProperties,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        logger.info("Using class '${vitalPlayersConfigurationProperties.playerClassName}' for new Vital managed player instances")
    }
}
