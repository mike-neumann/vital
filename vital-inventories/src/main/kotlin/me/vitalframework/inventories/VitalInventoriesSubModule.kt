package me.vitalframework.inventories

import me.vitalframework.VitalCoreSubModule.Companion.logger
import me.vitalframework.VitalSubModule
import org.springframework.stereotype.Component

@Component("vital-inventories")
class VitalInventoriesSubModule(
    val vitalInventories: List<VitalInventory>,
) : VitalSubModule() {
    val logger = logger()

    override fun onInstall() {
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (_: Exception) {
            logger.error(
                "'vital-inventories' has been installed, but the Bukkit runtime was not found on the server classpath, calling Bukkit APIs might fail.",
            )
            logger.error("Please make sure you are running 'vital-inventories' in the correct server environment, e.g. Spigot, Paper.")
        }

        for (vitalInventory in vitalInventories) {
            logger.info("Inventory '${vitalInventory::class.java.name}' successfully registered")
        }
    }
}
