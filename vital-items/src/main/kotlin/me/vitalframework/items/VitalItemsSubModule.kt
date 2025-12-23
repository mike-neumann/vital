package me.vitalframework.items

import me.vitalframework.SubModule
import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

/**
 * Defines the official vital-items submodule, which is displayed when Vital starts.
 * It contains the Vital item system, which can be used to create dynamic an interactable class-based items.
 */
@SubModule("vital-items")
class VitalItemsSubModule(
    val vitalItems: List<VitalItem>,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-items' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-items' in the correct server environment, e.g. Spigot, Paper.")
        }

        for (vitalItem in vitalItems) {
            logger.info("Item '${vitalItem::class.java.name}' successfully registered")
        }
    }
}
